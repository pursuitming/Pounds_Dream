package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.DashboardService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.DashboardVO;
import com.poundsdream.vo.WeightRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final DietRecordMapper dietRecordMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final BodyMeasurementMapper bodyMeasurementMapper;

    @Override
    public DashboardVO getDashboard() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        LocalDate today = LocalDate.now();

        // 今日体重
        WeightRecord todayWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, today));

        // 昨日体重
        WeightRecord yesterdayWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, today.minusDays(1)));

        BigDecimal todayWeightValue = todayWeight != null ? todayWeight.getWeight() : null;
        BigDecimal weightChange = null;
        if (todayWeight != null && yesterdayWeight != null) {
            weightChange = todayWeight.getWeight().subtract(yesterdayWeight.getWeight());
        }

        // BMI
        BigDecimal bmi = null;
        if (todayWeight != null && user != null && user.getHeight() != null) {
            bmi = CalorieCalculator.calcBMI(todayWeight.getWeight(), user.getHeight());
        }

        // 今日饮食热量
        List<DietRecord> todayDiets = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, today));

        int todayCalorieIntake = todayDiets.stream().mapToInt(DietRecord::getCalorie).sum();

        // 今日运动消耗
        List<ExerciseRecord> todayExercises = exerciseRecordMapper.selectList(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, today));

        int todayCalorieBurned = todayExercises.stream().mapToInt(ExerciseRecord::getCalorieBurned).sum();

        // 目标热量
        int targetCalorie = user != null && user.getTargetCalorie() != null ? user.getTargetCalorie() : 1800;

        // 连续打卡天数
        int streakDays = calcStreakDays(userId, today);

        // 近 7 天体重趋势
        List<WeightRecordVO> weekTrend = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .ge(WeightRecord::getRecordDate, today.minusDays(6))
                        .orderByAsc(WeightRecord::getRecordDate))
                .stream()
                .map(r -> {
                    WeightRecordVO vo = new WeightRecordVO();
                    BeanUtils.copyProperties(r, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        // 今日各餐热量
        Map<String, Integer> todayMeals = new LinkedHashMap<>();
        todayMeals.put("breakfast", 0);
        todayMeals.put("lunch", 0);
        todayMeals.put("dinner", 0);
        todayMeals.put("snack", 0);

        for (DietRecord record : todayDiets) {
            switch (record.getMealType()) {
                case 1 -> todayMeals.merge("breakfast", record.getCalorie(), Integer::sum);
                case 2 -> todayMeals.merge("lunch", record.getCalorie(), Integer::sum);
                case 3 -> todayMeals.merge("dinner", record.getCalorie(), Integer::sum);
                case 4 -> todayMeals.merge("snack", record.getCalorie(), Integer::sum);
            }
        }

        // 最新围度数据
        BodyMeasurement latestMeasurement = bodyMeasurementMapper.selectOne(
                new LambdaQueryWrapper<BodyMeasurement>()
                        .eq(BodyMeasurement::getUserId, userId)
                        .orderByDesc(BodyMeasurement::getRecordDate)
                        .last("LIMIT 1"));

        // 最新体脂率
        BigDecimal bodyFat = todayWeight != null && todayWeight.getBodyFat() != null
                ? todayWeight.getBodyFat() : null;

        return DashboardVO.builder()
                .todayWeight(todayWeightValue)
                .weightChange(weightChange)
                .targetWeight(user != null ? user.getTargetWeight() : null)
                .bmi(bmi)
                .todayCalorieIntake(todayCalorieIntake)
                .todayCalorieBurned(todayCalorieBurned)
                .targetCalorie(targetCalorie)
                .streakDays(streakDays)
                .weekWeightTrend(weekTrend)
                .todayMeals(todayMeals)
                .height(user != null ? user.getHeight() : null)
                .gender(user != null ? user.getGender() : null)
                .bodyFat(bodyFat)
                .chest(latestMeasurement != null ? latestMeasurement.getChest() : null)
                .waist(latestMeasurement != null ? latestMeasurement.getWaist() : null)
                .hip(latestMeasurement != null ? latestMeasurement.getHip() : null)
                .upperArm(latestMeasurement != null ? latestMeasurement.getUpperArm() : null)
                .thigh(latestMeasurement != null ? latestMeasurement.getThigh() : null)
                .build();
    }

    private int calcStreakDays(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate date = today;

        while (true) {
            boolean hasWeight = weightRecordMapper.selectCount(
                    new LambdaQueryWrapper<WeightRecord>()
                            .eq(WeightRecord::getUserId, userId)
                            .eq(WeightRecord::getRecordDate, date)) > 0;

            boolean hasDiet = dietRecordMapper.selectCount(
                    new LambdaQueryWrapper<DietRecord>()
                            .eq(DietRecord::getUserId, userId)
                            .eq(DietRecord::getRecordDate, date)) > 0;

            boolean hasExercise = exerciseRecordMapper.selectCount(
                    new LambdaQueryWrapper<ExerciseRecord>()
                            .eq(ExerciseRecord::getUserId, userId)
                            .eq(ExerciseRecord::getRecordDate, date)) > 0;

            if (hasWeight || hasDiet || hasExercise) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
