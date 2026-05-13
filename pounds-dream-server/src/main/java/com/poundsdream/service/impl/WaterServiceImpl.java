package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.dto.WaterRecordDTO;
import com.poundsdream.entity.User;
import com.poundsdream.entity.WaterGoal;
import com.poundsdream.entity.WaterRecord;
import com.poundsdream.entity.WeightRecord;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.mapper.WaterGoalMapper;
import com.poundsdream.mapper.WaterRecordMapper;
import com.poundsdream.mapper.WeightRecordMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.WaterService;
import com.poundsdream.vo.WaterDailyVO;
import com.poundsdream.vo.WaterDayStat;
import com.poundsdream.vo.WaterRecordVO;
import com.poundsdream.vo.WaterTypeStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterServiceImpl implements WaterService {

    private final WaterRecordMapper waterRecordMapper;
    private final WaterGoalMapper waterGoalMapper;
    private final UserMapper userMapper;
    private final WeightRecordMapper weightRecordMapper;

    @Override
    public void addRecord(WaterRecordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setAmount(dto.getAmount());
        record.setDrinkType(dto.getDrinkType() != null ? dto.getDrinkType() : "water");
        record.setRecordTime(LocalDateTime.now());
        record.setRecordDate(LocalDate.now());

        waterRecordMapper.insert(record);
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        WaterRecord record = waterRecordMapper.selectById(id);
        if (record != null && record.getUserId().equals(userId)) {
            waterRecordMapper.deleteById(id);
        }
    }

    @Override
    public WaterDailyVO getTodayData() {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();

        // 今日饮水量
        int todayAmount = waterRecordMapper.sumAmountByDate(userId, today);

        // 今日记录列表
        List<WaterRecord> records = waterRecordMapper.selectList(
                new LambdaQueryWrapper<WaterRecord>()
                        .eq(WaterRecord::getUserId, userId)
                        .eq(WaterRecord::getRecordDate, today)
                        .orderByDesc(WaterRecord::getRecordTime));

        List<WaterRecordVO> recordVOs = records.stream()
                .map(r -> WaterRecordVO.builder()
                        .id(r.getId())
                        .amount(r.getAmount())
                        .drinkType(r.getDrinkType())
                        .recordTime(r.getRecordTime())
                        .build())
                .collect(Collectors.toList());

        // 每日目标
        int dailyGoal = getDailyGoal(userId);

        // 过去7天数据
        LocalDate startDate = today.minusDays(6);
        List<Map<String, Object>> weekData = waterRecordMapper.sumAmountByDateRange(userId, startDate, today);

        Map<String, Integer> dateAmountMap = new HashMap<>();
        for (Map<String, Object> row : weekData) {
            dateAmountMap.put(row.get("recordDate").toString(), ((Number) row.get("totalAmount")).intValue());
        }

        List<WaterDayStat> recentDays = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            recentDays.add(WaterDayStat.builder()
                    .date(date)
                    .totalAmount(dateAmountMap.getOrDefault(date.toString(), 0))
                    .build());
        }

        int percentage = dailyGoal > 0 ? Math.min(100, Math.round((float) todayAmount / dailyGoal * 100)) : 0;
        int remaining = Math.max(0, dailyGoal - todayAmount);

        return WaterDailyVO.builder()
                .todayAmount(todayAmount)
                .dailyGoal(dailyGoal)
                .percentage(percentage)
                .remaining(remaining)
                .records(recordVOs)
                .recentDays(recentDays)
                .build();
    }

    @Override
    public void updateDailyGoal(Integer goal) {
        Long userId = SecurityUtil.getCurrentUserId();

        WaterGoal waterGoal = waterGoalMapper.selectOne(
                new LambdaQueryWrapper<WaterGoal>().eq(WaterGoal::getUserId, userId));

        if (waterGoal == null) {
            waterGoal = new WaterGoal();
            waterGoal.setUserId(userId);
            waterGoal.setDailyGoal(goal);
            waterGoalMapper.insert(waterGoal);
        } else {
            waterGoal.setDailyGoal(goal);
            waterGoalMapper.updateById(waterGoal);
        }
    }

    @Override
    public List<WaterTypeStat> getTypeStats(int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Map<String, Object>> data = waterRecordMapper.sumAmountByDrinkType(userId, startDate, today);

        int totalAmount = data.stream().mapToInt(row -> ((Number) row.get("totalAmount")).intValue()).sum();

        Map<String, String> typeNames = Map.of(
                "water", "白开水", "tea", "茶", "coffee", "咖啡", "other", "其他");

        return data.stream().map(row -> {
            int amount = ((Number) row.get("totalAmount")).intValue();
            String type = row.get("drinkType").toString();
            return WaterTypeStat.builder()
                    .drinkType(type)
                    .typeName(typeNames.getOrDefault(type, type))
                    .totalAmount(amount)
                    .percentage(totalAmount > 0 ? Math.round((float) amount / totalAmount * 100) : 0)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<WaterDayStat> getTrendData(int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Map<String, Object>> data = waterRecordMapper.sumAmountByDateRange(userId, startDate, today);

        Map<String, Integer> dateAmountMap = new HashMap<>();
        for (Map<String, Object> row : data) {
            dateAmountMap.put(row.get("recordDate").toString(), ((Number) row.get("totalAmount")).intValue());
        }

        List<WaterDayStat> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            result.add(WaterDayStat.builder()
                    .date(date)
                    .totalAmount(dateAmountMap.getOrDefault(date.toString(), 0))
                    .build());
        }
        return result;
    }

    private int getDailyGoal(Long userId) {
        // 先查用户自定义目标
        WaterGoal waterGoal = waterGoalMapper.selectOne(
                new LambdaQueryWrapper<WaterGoal>().eq(WaterGoal::getUserId, userId));
        if (waterGoal != null) {
            return waterGoal.getDailyGoal();
        }

        // 智能推荐：体重 × 35ml
        WeightRecord latestWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .orderByDesc(WeightRecord::getRecordDate)
                        .last("LIMIT 1"));

        if (latestWeight != null && latestWeight.getWeight() != null) {
            return latestWeight.getWeight().multiply(BigDecimal.valueOf(35)).intValue();
        }

        // 默认 2000ml
        return 2000;
    }
}
