package com.poundsdream.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsTask {

    private final UserMapper userMapper;
    private final DietRecordMapper dietRecordMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final DailyStatsMapper dailyStatsMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始每日统计任务，日期: {}", yesterday);

        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        for (User user : users) {
            try {
                processUserStats(user, yesterday);
            } catch (Exception e) {
                log.error("用户 {} 统计失败", user.getId(), e);
            }
        }
        log.info("每日统计任务完成，共处理 {} 个用户", users.size());
    }

    private void processUserStats(User user, LocalDate date) {
        Long userId = user.getId();

        // 饮食统计
        List<DietRecord> dietRecords = dietRecordMapper.selectList(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date));
        int totalIntake = dietRecords.stream().mapToInt(DietRecord::getCalorie).sum();

        // 运动统计
        List<ExerciseRecord> exerciseRecords = exerciseRecordMapper.selectList(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, date));
        int totalBurned = exerciseRecords.stream().mapToInt(ExerciseRecord::getCalorieBurned).sum();

        // 体重
        WeightRecord wr = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, date));

        int targetCalorie = user.getTargetCalorie() != null ? user.getTargetCalorie() : 1800;

        // 存在则更新，否则插入
        DailyStats existing = dailyStatsMapper.selectOne(
                new LambdaQueryWrapper<DailyStats>()
                        .eq(DailyStats::getUserId, userId)
                        .eq(DailyStats::getStatDate, date));

        if (existing != null) {
            existing.setTotalIntake(totalIntake);
            existing.setTotalBurned(totalBurned);
            existing.setTargetCalorie(targetCalorie);
            existing.setDietCount(dietRecords.size());
            existing.setExerciseCount(exerciseRecords.size());
            existing.setWeight(wr != null ? wr.getWeight() : null);
            existing.setIsGoalMet(totalIntake <= targetCalorie ? 1 : 0);
            dailyStatsMapper.updateById(existing);
        } else {
            DailyStats stats = new DailyStats();
            stats.setUserId(userId);
            stats.setStatDate(date);
            stats.setTotalIntake(totalIntake);
            stats.setTotalBurned(totalBurned);
            stats.setTargetCalorie(targetCalorie);
            stats.setProtein(BigDecimal.ZERO);
            stats.setFat(BigDecimal.ZERO);
            stats.setCarbohydrate(BigDecimal.ZERO);
            stats.setDietCount(dietRecords.size());
            stats.setExerciseCount(exerciseRecords.size());
            stats.setWeight(wr != null ? wr.getWeight() : null);
            stats.setIsGoalMet(totalIntake <= targetCalorie ? 1 : 0);
            dailyStatsMapper.insert(stats);
        }
    }
}
