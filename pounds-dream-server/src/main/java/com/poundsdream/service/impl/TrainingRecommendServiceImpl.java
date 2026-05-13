package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.entity.ExerciseType;
import com.poundsdream.entity.User;
import com.poundsdream.entity.WeightRecord;
import com.poundsdream.mapper.ExerciseTypeMapper;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.mapper.WeightRecordMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.TrainingRecommendService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.RecommendedTrainingVO;
import com.poundsdream.vo.RecommendedTrainingVO.RecommendedExercise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainingRecommendServiceImpl implements TrainingRecommendService {

    private final ExerciseTypeMapper exerciseTypeMapper;
    private final UserMapper userMapper;
    private final WeightRecordMapper weightRecordMapper;

    @Override
    public RecommendedTrainingVO getRecommendedTraining(String intensity) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (user == null || user.getHeight() == null || user.getGender() == null) {
            return RecommendedTrainingVO.builder()
                    .calorieToBurn(BigDecimal.ZERO)
                    .exercises(Collections.emptyList())
                    .totalDuration(0)
                    .intensity(intensity != null ? intensity : "MEDIUM")
                    .build();
        }

        // 获取最新体重
        WeightRecord latestWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .orderByDesc(WeightRecord::getRecordDate)
                        .last("LIMIT 1"));

        BigDecimal weight = latestWeight != null ? latestWeight.getWeight() : user.getTargetWeight();
        if (weight == null) weight = BigDecimal.valueOf(65);

        // 计算基础数据
        int age = CalorieCalculator.calcAge(user.getBirthday());
        int gender = user.getGender() == 1 ? 1 : 0;
        BigDecimal bmr = CalorieCalculator.calcBMR(user.getGender(), weight, user.getHeight(), age);

        // 计算TDEE（默认中等活动量）
        int activityLevel = user.getActivityLevel() != null ? user.getActivityLevel() : 2;
        double activityFactor = getActivityFactor(activityLevel);
        BigDecimal tdee = bmr.multiply(BigDecimal.valueOf(activityFactor)).setScale(0, RoundingMode.HALF_UP);

        // 目标热量（默认每日缺口500kcal，一周减0.5kg）
        BigDecimal targetCalorie = user.getTargetCalorie() != null
                ? BigDecimal.valueOf(user.getTargetCalorie())
                : tdee.subtract(BigDecimal.valueOf(500));

        // 需要通过运动消耗的热量（TDEE - 目标摄入 + 一些缓冲）
        BigDecimal calorieToBurn = tdee.subtract(targetCalorie);
        if (calorieToBurn.compareTo(BigDecimal.ZERO) <= 0) {
            calorieToBurn = BigDecimal.valueOf(200); // 最少推荐200kcal运动
        }

        // 根据强度调整
        if (intensity == null) intensity = "MEDIUM";
        switch (intensity.toUpperCase()) {
            case "LOW":
                calorieToBurn = calorieToBurn.multiply(BigDecimal.valueOf(0.6));
                break;
            case "HIGH":
                calorieToBurn = calorieToBurn.multiply(BigDecimal.valueOf(1.4));
                break;
            default: // MEDIUM
                break;
        }

        // 获取所有运动类型
        List<ExerciseType> allExercises = exerciseTypeMapper.selectList(
                new LambdaQueryWrapper<ExerciseType>().isNotNull(ExerciseType::getCaloriePerMin));

        // 按类别分组
        List<ExerciseType> aerobic = new ArrayList<>(); // 有氧
        List<ExerciseType> strength = new ArrayList<>(); // 力量
        List<ExerciseType> flexibility = new ArrayList<>(); // 拉伸

        for (ExerciseType ex : allExercises) {
            String category = ex.getCategory() != null ? ex.getCategory() : "";
            if (category.contains("有氧") || category.contains("跑步") || category.contains("游泳") || category.contains("跳绳")) {
                aerobic.add(ex);
            } else if (category.contains("力量") || category.contains("器械") || category.contains("深蹲") || category.contains("俯卧撑")) {
                strength.add(ex);
            } else if (category.contains("拉伸") || category.contains("瑜伽") || category.contains("放松")) {
                flexibility.add(ex);
            } else {
                // 默认归类为有氧
                aerobic.add(ex);
            }
        }

        // 如果分类为空，使用默认分类
        if (aerobic.isEmpty()) aerobic = allExercises.subList(0, Math.min(5, allExercises.size()));
        if (strength.isEmpty() && allExercises.size() > 5) strength = allExercises.subList(5, Math.min(8, allExercises.size()));
        if (flexibility.isEmpty() && allExercises.size() > 8) flexibility = allExercises.subList(8, Math.min(10, allExercises.size()));

        // 分配热量：有氧60% + 力量30% + 拉伸10%
        BigDecimal aerobicCalorie = calorieToBurn.multiply(BigDecimal.valueOf(0.6));
        BigDecimal strengthCalorie = calorieToBurn.multiply(BigDecimal.valueOf(0.3));
        BigDecimal flexibilityCalorie = calorieToBurn.multiply(BigDecimal.valueOf(0.1));

        List<RecommendedExercise> recommendedExercises = new ArrayList<>();
        int totalDuration = 0;

        // 推荐有氧运动（1-2种）
        if (!aerobic.isEmpty()) {
            ExerciseType mainAerobic = aerobic.get(0);
            int duration = calcDuration(mainAerobic, aerobicCalorie);
            totalDuration += duration;
            recommendedExercises.add(RecommendedExercise.builder()
                    .exerciseTypeId(mainAerobic.getId())
                    .name(mainAerobic.getName())
                    .category("有氧")
                    .duration(duration)
                    .calorieBurn(calcCalorie(mainAerobic, duration))
                    .metValue(mainAerobic.getMetValue())
                    .description(mainAerobic.getDescription())
                    .priority(1)
                    .build());

            // 第二种有氧（可选）
            if (aerobic.size() > 1 && aerobicCalorie.compareTo(BigDecimal.valueOf(200)) > 0) {
                ExerciseType secondAerobic = aerobic.get(1);
                int dur2 = Math.max(10, duration / 2);
                totalDuration += dur2;
                recommendedExercises.add(RecommendedExercise.builder()
                        .exerciseTypeId(secondAerobic.getId())
                        .name(secondAerobic.getName())
                        .category("有氧")
                        .duration(dur2)
                        .calorieBurn(calcCalorie(secondAerobic, dur2))
                        .metValue(secondAerobic.getMetValue())
                        .description(secondAerobic.getDescription())
                        .priority(3)
                        .build());
            }
        }

        // 推荐力量训练（1-2种）
        if (!strength.isEmpty()) {
            ExerciseType mainStrength = strength.get(0);
            int duration = calcDuration(mainStrength, strengthCalorie);
            totalDuration += duration;
            recommendedExercises.add(RecommendedExercise.builder()
                    .exerciseTypeId(mainStrength.getId())
                    .name(mainStrength.getName())
                    .category("力量")
                    .duration(duration)
                    .calorieBurn(calcCalorie(mainStrength, duration))
                    .metValue(mainStrength.getMetValue())
                    .description(mainStrength.getDescription())
                    .priority(2)
                    .build());

            if (strength.size() > 1) {
                ExerciseType secondStrength = strength.get(1);
                int dur2 = Math.max(10, duration / 2);
                totalDuration += dur2;
                recommendedExercises.add(RecommendedExercise.builder()
                        .exerciseTypeId(secondStrength.getId())
                        .name(secondStrength.getName())
                        .category("力量")
                        .duration(dur2)
                        .calorieBurn(calcCalorie(secondStrength, dur2))
                        .metValue(secondStrength.getMetValue())
                        .description(secondStrength.getDescription())
                        .priority(3)
                        .build());
            }
        }

        // 推荐拉伸放松（1种）
        if (!flexibility.isEmpty()) {
            ExerciseType mainFlex = flexibility.get(0);
            int duration = calcDuration(mainFlex, flexibilityCalorie);
            duration = Math.max(10, Math.min(20, duration)); // 拉伸10-20分钟
            totalDuration += duration;
            recommendedExercises.add(RecommendedExercise.builder()
                    .exerciseTypeId(mainFlex.getId())
                    .name(mainFlex.getName())
                    .category("拉伸")
                    .duration(duration)
                    .calorieBurn(calcCalorie(mainFlex, duration))
                    .metValue(mainFlex.getMetValue())
                    .description(mainFlex.getDescription())
                    .priority(2)
                    .build());
        }

        return RecommendedTrainingVO.builder()
                .tdee(tdee)
                .todayIntake(BigDecimal.ZERO) // 前端可以传入今日已摄入
                .targetCalorie(targetCalorie)
                .calorieToBurn(calorieToBurn.setScale(0, RoundingMode.HALF_UP))
                .exercises(recommendedExercises)
                .totalDuration(totalDuration)
                .intensity(intensity)
                .build();
    }

    /**
     * 根据运动类型和目标消耗计算时长
     */
    private int calcDuration(ExerciseType exercise, BigDecimal targetCalorie) {
        if (exercise.getCaloriePerMin() == null || exercise.getCaloriePerMin().compareTo(BigDecimal.ZERO) <= 0) {
            return 30; // 默认30分钟
        }
        int duration = targetCalorie.divide(exercise.getCaloriePerMin(), 0, RoundingMode.CEILING).intValue();
        return Math.max(15, Math.min(60, duration)); // 限制在15-60分钟
    }

    /**
     * 计算运动消耗热量
     */
    private BigDecimal calcCalorie(ExerciseType exercise, int duration) {
        if (exercise.getCaloriePerMin() == null) {
            return BigDecimal.ZERO;
        }
        return exercise.getCaloriePerMin().multiply(BigDecimal.valueOf(duration)).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 获取活动系数
     */
    private double getActivityFactor(int level) {
        switch (level) {
            case 1: return 1.2;  // 久坐不动
            case 2: return 1.375; // 轻度活动
            case 3: return 1.55;  // 中度活动
            case 4: return 1.725; // 重度活动
            default: return 1.375;
        }
    }
}
