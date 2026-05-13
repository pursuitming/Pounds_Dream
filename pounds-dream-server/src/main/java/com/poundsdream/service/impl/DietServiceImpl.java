package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.DietRecordDTO;
import com.poundsdream.dto.SaveTemplateDTO;
import com.poundsdream.entity.*;
import com.poundsdream.enums.MealType;
import com.poundsdream.mapper.*;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.DietService;
import com.poundsdream.service.RankService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.DailyDietVO;
import com.poundsdream.vo.DietRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietServiceImpl implements DietService {

    private final DietRecordMapper dietRecordMapper;
    private final FoodMapper foodMapper;
    private final UserMapper userMapper;
    private final DietTemplateMapper dietTemplateMapper;
    private final DietTemplateItemMapper dietTemplateItemMapper;
    private final MealStatusMapper mealStatusMapper;
    private final RankService rankService;

    @Override
    public DietRecordVO addRecord(DietRecordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate recordDate = dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now();

        Food food = foodMapper.selectById(dto.getFoodId());
        if (food == null) {
            throw new BusinessException(ErrorCode.FOOD_NOT_FOUND);
        }

        int calorie = CalorieCalculator.calcFoodCalorie(food.getCalorie(), dto.getAmount().doubleValue());

        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setFoodId(dto.getFoodId());
        record.setMealType(dto.getMealType());
        record.setAmount(dto.getAmount());
        record.setCalorie(calorie);
        record.setRecordDate(recordDate);
        dietRecordMapper.insert(record);

        // 更新餐次状态为已记录
        updateMealStatus(userId, recordDate, dto.getMealType(), "RECORDED");

        // 触发段位打卡检查
        try { rankService.checkIn(userId); } catch (Exception e) { log.warn("段位打卡检查失败", e); }

        return convertToVO(record, food);
    }

    @Override
    public DailyDietVO getDailyDiet(LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (date == null) date = LocalDate.now();

        List<DietRecord> records = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date)
                        .orderByAsc(DietRecord::getMealType));

        // 获取所有涉及的食物
        Set<Long> foodIds = records.stream().map(DietRecord::getFoodId).collect(Collectors.toSet());
        Map<Long, Food> foodMap = new HashMap<>();
        if (!foodIds.isEmpty()) {
            List<Food> foods = foodMapper.selectBatchIds(foodIds);
            foodMap = foods.stream().collect(Collectors.toMap(Food::getId, f -> f));
        }

        // 转换 VO 并按餐次分组
        Map<String, List<DietRecordVO>> mealsByType = new LinkedHashMap<>();
        mealsByType.put("breakfast", new ArrayList<>());
        mealsByType.put("lunch", new ArrayList<>());
        mealsByType.put("dinner", new ArrayList<>());
        mealsByType.put("snack", new ArrayList<>());

        int totalCalorie = 0;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;

        for (DietRecord record : records) {
            Food food = foodMap.get(record.getFoodId());
            DietRecordVO vo = convertToVO(record, food);

            MealType mealType = MealType.fromCode(record.getMealType());
            switch (mealType) {
                case BREAKFAST -> mealsByType.get("breakfast").add(vo);
                case LUNCH -> mealsByType.get("lunch").add(vo);
                case DINNER -> mealsByType.get("dinner").add(vo);
                case SNACK -> mealsByType.get("snack").add(vo);
            }

            totalCalorie += record.getCalorie();
            if (food != null) {
                BigDecimal ratio = record.getAmount().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
                totalProtein = totalProtein.add(food.getProtein().multiply(ratio));
                totalFat = totalFat.add(food.getFat().multiply(ratio));
                totalCarb = totalCarb.add(food.getCarbohydrate().multiply(ratio));
            }
        }

        // 获取目标热量
        User user = userMapper.selectById(userId);
        int targetCalorie = user != null && user.getTargetCalorie() != null ? user.getTargetCalorie() : 1800;

        DailyDietVO vo = new DailyDietVO();
        vo.setTotalCalorie(totalCalorie);
        vo.setTargetCalorie(targetCalorie);
        vo.setRemainingCalorie(targetCalorie - totalCalorie);
        vo.setTotalProtein(totalProtein.setScale(1, java.math.RoundingMode.HALF_UP));
        vo.setTotalFat(totalFat.setScale(1, java.math.RoundingMode.HALF_UP));
        vo.setTotalCarbohydrate(totalCarb.setScale(1, java.math.RoundingMode.HALF_UP));
        vo.setMealsByType(mealsByType);
        return vo;
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        DietRecord record = dietRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        dietRecordMapper.deleteById(id);
    }

    @Override
    public List<Food> searchFood(String keyword) {
        return foodMapper.selectList(
                new LambdaQueryWrapper<Food>()
                        .like(Food::getName, keyword)
                        .last("LIMIT 20"));
    }

    @Override
    public Food addCustomFood(Food food) {
        Long userId = SecurityUtil.getCurrentUserId();
        food.setIsCustom(1);
        food.setCreatorId(userId);
        foodMapper.insert(food);
        return food;
    }

    private DietRecordVO convertToVO(DietRecord record, Food food) {
        DietRecordVO vo = new DietRecordVO();
        vo.setId(record.getId());
        vo.setFoodId(record.getFoodId());
        vo.setMealType(record.getMealType());
        vo.setMealTypeName(MealType.fromCode(record.getMealType()).getDesc());
        vo.setAmount(record.getAmount());
        vo.setCalorie(record.getCalorie());
        vo.setRecordDate(record.getRecordDate());
        if (food != null) {
            vo.setFoodName(food.getName());
            BigDecimal ratio = record.getAmount().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            vo.setProtein(food.getProtein().multiply(ratio).setScale(1, java.math.RoundingMode.HALF_UP));
            vo.setFat(food.getFat().multiply(ratio).setScale(1, java.math.RoundingMode.HALF_UP));
            vo.setCarbohydrate(food.getCarbohydrate().multiply(ratio).setScale(1, java.math.RoundingMode.HALF_UP));
            vo.setRiskLevel(calcRiskLevel(food));
            vo.setRiskTags(calcRiskTags(food));
        }
        return vo;
    }

    private String calcRiskLevel(Food food) {
        int riskCount = 0;
        if (food.getFat() != null && food.getFat().doubleValue() > 20) riskCount++;
        if (food.getCarbohydrate() != null && food.getCarbohydrate().doubleValue() > 50) riskCount++;
        if (food.getGi() != null && food.getGi().doubleValue() > 70) riskCount++;
        if (food.getCalorie() > 300) riskCount++;
        if (riskCount >= 2) return "HIGH";
        if (riskCount == 1) return "MEDIUM";
        return "LOW";
    }

    private List<String> calcRiskTags(Food food) {
        List<String> tags = new ArrayList<>();
        if (food.getFat() != null && food.getFat().doubleValue() > 20) tags.add("高油");
        if (food.getCarbohydrate() != null && food.getCarbohydrate().doubleValue() > 50) tags.add("高糖");
        if (food.getGi() != null && food.getGi().doubleValue() > 70) tags.add("高GI");
        if (food.getCalorie() > 300) tags.add("高热量");
        return tags;
    }

    @Override
    public List<Map<String, Object>> getAlternatives(Long foodId, int limit) {
        Food food = foodMapper.selectById(foodId);
        if (food == null) {
            throw new BusinessException(ErrorCode.FOOD_NOT_FOUND);
        }
        List<Food> candidates = foodMapper.selectList(
                new LambdaQueryWrapper<Food>()
                        .eq(Food::getCategory, food.getCategory())
                        .ne(Food::getId, foodId)
                        .last("LIMIT 50"));

        // 按健康评分排序：热量低+蛋白质高+GI低 = 更健康
        candidates.sort((a, b) -> {
            double scoreA = calcHealthScore(a, food);
            double scoreB = calcHealthScore(b, food);
            return Double.compare(scoreB, scoreA);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (Food f : candidates.subList(0, Math.min(limit, candidates.size()))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", f.getId());
            item.put("name", f.getName());
            item.put("category", f.getCategory());
            item.put("calorie", f.getCalorie());
            item.put("protein", f.getProtein());
            item.put("fat", f.getFat());
            item.put("carbohydrate", f.getCarbohydrate());
            item.put("gi", f.getGi());
            item.put("reason", buildAlternativeReason(f, food));
            result.add(item);
        }
        return result;
    }

    private double calcHealthScore(Food candidate, Food original) {
        double score = 0;
        // 热量更低加分
        if (candidate.getCalorie() < original.getCalorie()) score += 30;
        // 蛋白质更高加分
        if (candidate.getProtein() != null && original.getProtein() != null
                && candidate.getProtein().doubleValue() > original.getProtein().doubleValue()) score += 30;
        // GI更低加分
        if (candidate.getGi() != null && original.getGi() != null
                && candidate.getGi().doubleValue() < original.getGi().doubleValue()) score += 20;
        // 脂肪更低加分
        if (candidate.getFat() != null && original.getFat() != null
                && candidate.getFat().doubleValue() < original.getFat().doubleValue()) score += 20;
        return score;
    }

    private String buildAlternativeReason(Food candidate, Food original) {
        StringBuilder sb = new StringBuilder();
        if (candidate.getCalorie() < original.getCalorie()) {
            sb.append("热量低").append(original.getCalorie() - candidate.getCalorie()).append("kcal ");
        }
        if (candidate.getProtein() != null && original.getProtein() != null
                && candidate.getProtein().doubleValue() > original.getProtein().doubleValue()) {
            sb.append("蛋白质更高 ");
        }
        if (candidate.getGi() != null && original.getGi() != null
                && candidate.getGi().doubleValue() < original.getGi().doubleValue()) {
            sb.append("GI更低 ");
        }
        return sb.length() > 0 ? sb.toString().trim() : "同类食物";
    }

    @Override
    public List<Map<String, Object>> getRecommendations() {
        Long userId = SecurityUtil.getCurrentUserId();
        DailyDietVO daily = getDailyDiet(LocalDate.now());
        int remaining = daily.getRemainingCalorie() != null ? daily.getRemainingCalorie() : 0;
        BigDecimal targetProtein = BigDecimal.valueOf((daily.getTargetCalorie() != null ? daily.getTargetCalorie() : 1800) * 0.3 / 4);
        BigDecimal currentProtein = daily.getTotalProtein() != null ? daily.getTotalProtein() : BigDecimal.ZERO;
        BigDecimal currentFat = daily.getTotalFat() != null ? daily.getTotalFat() : BigDecimal.ZERO;
        BigDecimal targetFat = BigDecimal.valueOf((daily.getTargetCalorie() != null ? daily.getTargetCalorie() : 1800) * 0.3 / 9);

        List<Food> allFoods = foodMapper.selectList(new LambdaQueryWrapper<Food>().last("LIMIT 200"));
        List<Map<String, Object>> result = new ArrayList<>();
        String reason;

        if (remaining < 200) {
            reason = "今日剩余热量较少，推荐低热量食物";
            allFoods.stream()
                    .filter(f -> f.getCalorie() <= 100)
                    .sorted(Comparator.comparingInt(Food::getCalorie))
                    .limit(5)
                    .forEach(f -> result.add(buildRecommendItem(f, reason)));
        } else if (remaining < 500) {
            reason = "适量摄入，推荐均衡食物";
            allFoods.stream()
                    .filter(f -> f.getCalorie() >= 100 && f.getCalorie() <= 250)
                    .sorted((a, b) -> {
                        double sa = a.getProtein() != null ? a.getProtein().doubleValue() : 0;
                        double sb = b.getProtein() != null ? b.getProtein().doubleValue() : 0;
                        return Double.compare(sb, sa);
                    })
                    .limit(5)
                    .forEach(f -> result.add(buildRecommendItem(f, reason)));
        } else {
            reason = "剩余热量充足，推荐高蛋白食物";
            allFoods.stream()
                    .filter(f -> f.getProtein() != null && f.getProtein().doubleValue() >= 15)
                    .sorted((a, b) -> Double.compare(
                            b.getProtein() != null ? b.getProtein().doubleValue() : 0,
                            a.getProtein() != null ? a.getProtein().doubleValue() : 0))
                    .limit(5)
                    .forEach(f -> result.add(buildRecommendItem(f, reason)));
        }

        // 蛋白质不足时额外推荐
        if (currentProtein.compareTo(targetProtein.multiply(BigDecimal.valueOf(0.7))) < 0) {
            String proteinReason = "蛋白质摄入不足，建议补充";
            allFoods.stream()
                    .filter(f -> f.getProtein() != null && f.getProtein().doubleValue() >= 20)
                    .filter(f -> result.stream().noneMatch(r -> r.get("id").equals(f.getId())))
                    .sorted((a, b) -> Double.compare(
                            b.getProtein() != null ? b.getProtein().doubleValue() : 0,
                            a.getProtein() != null ? a.getProtein().doubleValue() : 0))
                    .limit(3)
                    .forEach(f -> result.add(buildRecommendItem(f, proteinReason)));
        }

        // 脂肪超标时排除高脂
        if (currentFat.compareTo(targetFat) > 0) {
            result.removeIf(r -> {
                Object fat = r.get("fat");
                return fat != null && ((BigDecimal) fat).doubleValue() > 15;
            });
        }

        return result;
    }

    private Map<String, Object> buildRecommendItem(Food food, String reason) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", food.getId());
        item.put("name", food.getName());
        item.put("category", food.getCategory());
        item.put("calorie", food.getCalorie());
        item.put("protein", food.getProtein());
        item.put("fat", food.getFat());
        item.put("carbohydrate", food.getCarbohydrate());
        item.put("reason", reason);
        return item;
    }

    // ========== 饮食模版 ==========

    @Override
    @Transactional
    public Map<String, Object> saveAsTemplate(SaveTemplateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 获取当天该餐次的记录
        List<DietRecord> records = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, LocalDate.now())
                        .eq(DietRecord::getMealType, dto.getMealType()));

        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        int totalCalorie = records.stream().mapToInt(DietRecord::getCalorie).sum();

        // 创建模版
        DietTemplate template = new DietTemplate();
        template.setUserId(userId);
        template.setName(dto.getName());
        template.setMealType(dto.getMealType());
        template.setTotalCalorie(totalCalorie);
        dietTemplateMapper.insert(template);

        // 创建模版明细
        for (DietRecord record : records) {
            DietTemplateItem item = new DietTemplateItem();
            item.setTemplateId(template.getId());
            item.setFoodId(record.getFoodId());
            item.setAmount(record.getAmount());
            dietTemplateItemMapper.insert(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", template.getId());
        result.put("name", template.getName());
        result.put("mealType", template.getMealType());
        result.put("totalCalorie", totalCalorie);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTemplates() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<DietTemplate> templates = dietTemplateMapper.selectList(
                new LambdaQueryWrapper<DietTemplate>()
                        .eq(DietTemplate::getUserId, userId)
                        .orderByDesc(DietTemplate::getCreatedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (DietTemplate tpl : templates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tpl.getId());
            item.put("name", tpl.getName());
            item.put("mealType", tpl.getMealType());
            item.put("mealTypeKey", getMealTypeKey(tpl.getMealType()));
            item.put("totalCalorie", tpl.getTotalCalorie());

            // 查询模版明细
            List<DietTemplateItem> items = dietTemplateItemMapper.selectList(
                    new LambdaQueryWrapper<DietTemplateItem>()
                            .eq(DietTemplateItem::getTemplateId, tpl.getId()));
            List<Map<String, Object>> itemDetails = new ArrayList<>();
            for (DietTemplateItem di : items) {
                Food food = foodMapper.selectById(di.getFoodId());
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("foodId", di.getFoodId());
                detail.put("foodName", food != null ? food.getName() : "未知食物");
                detail.put("amount", di.getAmount());
                itemDetails.add(detail);
            }
            item.put("items", itemDetails);
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        DietTemplate template = dietTemplateMapper.selectById(id);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        dietTemplateMapper.deleteById(id);
        dietTemplateItemMapper.delete(
                new LambdaQueryWrapper<DietTemplateItem>()
                        .eq(DietTemplateItem::getTemplateId, id));
    }

    @Override
    @Transactional
    public void applyTemplate(Long templateId, LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        DietTemplate template = dietTemplateMapper.selectById(templateId);
        if (template == null || !template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        List<DietTemplateItem> items = dietTemplateItemMapper.selectList(
                new LambdaQueryWrapper<DietTemplateItem>()
                        .eq(DietTemplateItem::getTemplateId, templateId));

        for (DietTemplateItem item : items) {
            Food food = foodMapper.selectById(item.getFoodId());
            if (food == null) continue;
            int calorie = CalorieCalculator.calcFoodCalorie(food.getCalorie(), item.getAmount().doubleValue());

            DietRecord record = new DietRecord();
            record.setUserId(userId);
            record.setFoodId(item.getFoodId());
            record.setMealType(template.getMealType());
            record.setAmount(item.getAmount());
            record.setCalorie(calorie);
            record.setRecordDate(date != null ? date : LocalDate.now());
            dietRecordMapper.insert(record);
        }
    }

    private String getMealTypeKey(int mealType) {
        return switch (mealType) {
            case 1 -> "breakfast";
            case 2 -> "lunch";
            case 3 -> "dinner";
            case 4 -> "snack";
            default -> "unknown";
        };
    }

    private void updateMealStatus(Long userId, LocalDate date, int mealType, String status) {
        MealStatus mealStatus = mealStatusMapper.selectOne(
                new LambdaQueryWrapper<MealStatus>()
                        .eq(MealStatus::getUserId, userId)
                        .eq(MealStatus::getMealDate, date)
                        .eq(MealStatus::getMealType, mealType));
        if (mealStatus != null) {
            mealStatus.setStatus(status);
            mealStatusMapper.updateById(mealStatus);
        } else {
            mealStatus = new MealStatus();
            mealStatus.setUserId(userId);
            mealStatus.setMealDate(date);
            mealStatus.setMealType(mealType);
            mealStatus.setStatus(status);
            mealStatusMapper.insert(mealStatus);
        }
    }

    @Override
    public void skipMeal(Integer mealType, LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate mealDate = date != null ? date : LocalDate.now();
        updateMealStatus(userId, mealDate, mealType, "SKIPPED");
    }
}
