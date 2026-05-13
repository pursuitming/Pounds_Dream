package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import com.poundsdream.service.RankService;
import com.poundsdream.vo.RankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private final UserRankMapper userRankMapper;
    private final DietRecordMapper dietRecordMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final WeightRecordMapper weightRecordMapper;

    // 段位配置: level -> {name, enName, days}
    private static final Map<Integer, String[]> RANK_CONFIG = Map.of(
            1, new String[]{"觉醒学徒", "Awakening Apprentice", "0"},
            2, new String[]{"燃脂工匠", "Fat-Burn Artisan", "5"},
            3, new String[]{"塑形行者", "Shape Walker", "10"},
            4, new String[]{"代谢引擎", "Metabolic Engine", "20"},
            5, new String[]{"肌理雕刻家", "Muscle Sculptor", "30"},
            6, new String[]{"巅峰领航员", "Peak Navigator", "60"},
            7, new String[]{"自律终结者", "Discipline Terminator", "100"}
    );

    // 里程碑积分: 天数 -> 积分
    private static final Map<Integer, Integer> MILESTONE_POINTS = Map.of(
            5, 50,
            10, 100,
            20, 200,
            30, 300,
            60, 600,
            100, 1000
    );

    // 段位图标
    private static final Map<Integer, String> RANK_ICONS = Map.of(
            1, "🌱",
            2, "⚒️",
            3, "🏃",
            4, "🔥",
            5, "💎",
            6, "🚀",
            7, "👑"
    );

    @Override
    public RankVO getRankInfo(Long userId) {
        UserRank rank = getOrCreateRank(userId);
        return buildRankVO(rank);
    }

    @Override
    @Transactional
    public RankVO checkIn(Long userId) {
        UserRank rank = getOrCreateRank(userId);
        LocalDate today = LocalDate.now();

        log.info("checkIn 用户={}, rankId={}, 当前积分={}, lastCheckDate={}, today={}", userId, rank.getId(), rank.getPoints(), rank.getLastCheckDate(), today);

        // 今天已经打过卡，直接返回
        if (rank.getLastCheckDate() != null && rank.getLastCheckDate().equals(today)) {
            log.info("今日已打卡，返回当前积分={}", rank.getPoints());
            return buildRankVO(rank);
        }

        // 判断是否有今日记录（饮食/运动/体重任意一项）
        boolean hasRecord = hasAnyRecordToday(userId);
        log.info("用户 {} 今日是否有记录: {}", userId, hasRecord);
        if (!hasRecord) {
            return buildRankVO(rank);
        }

        // 计算连续天数
        int newStreak;
        if (rank.getLastCheckDate() != null && rank.getLastCheckDate().equals(today.minusDays(1))) {
            // 昨天也打卡了，连续天数+1
            newStreak = rank.getCurrentStreak() + 1;
        } else if (rank.getLastCheckDate() != null && rank.getLastCheckDate().equals(today)) {
            // 今天已打卡（不应该到这里）
            return buildRankVO(rank);
        } else {
            // 断签了，重新开始
            newStreak = 1;
        }

        // 更新连续天数
        rank.setCurrentStreak(newStreak);
        rank.setLastCheckDate(today);

        // 更新历史最高
        if (newStreak > rank.getMaxStreak()) {
            rank.setMaxStreak(newStreak);
        }

        // 每日打卡积分 + 里程碑积分
        int dailyPoints = 10;
        int milestonePoints = checkMilestone(rank, newStreak);
        int oldPoints = rank.getPoints();
        int newPoints = oldPoints + dailyPoints + milestonePoints;
        rank.setPoints(newPoints);

        log.info("用户 {} 积分更新: {} -> {} (每日{}, 里程碑{})", userId, oldPoints, newPoints, dailyPoints, milestonePoints);

        // 更新段位（只升不降）
        int newLevel = calcRankLevel(rank.getMaxStreak());
        if (newLevel > rank.getRankLevel()) {
            rank.setRankLevel(newLevel);
            rank.setRankName(RANK_CONFIG.get(newLevel)[0]);
            log.info("用户 {} 升级到段位: {}", userId, rank.getRankName());
        }

        // 使用显式 UpdateWrapper 更新，避免 updateById 可能的填充干扰
        int rows = userRankMapper.update(null,
                new LambdaUpdateWrapper<UserRank>()
                        .eq(UserRank::getId, rank.getId())
                        .set(UserRank::getPoints, rank.getPoints())
                        .set(UserRank::getCurrentStreak, rank.getCurrentStreak())
                        .set(UserRank::getMaxStreak, rank.getMaxStreak())
                        .set(UserRank::getLastCheckDate, rank.getLastCheckDate())
                        .set(UserRank::getRankLevel, rank.getRankLevel())
                        .set(UserRank::getRankName, rank.getRankName()));
        log.info("用户 {} update 返回影响行数: {}", userId, rows);

        return buildRankVO(rank);
    }

    private boolean hasAnyRecordToday(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查饮食记录
        Long dietCount = dietRecordMapper.selectCount(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, today));
        if (dietCount > 0) return true;

        // 检查运动记录
        Long exerciseCount = exerciseRecordMapper.selectCount(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, today));
        if (exerciseCount > 0) return true;

        // 检查体重记录
        Long weightCount = weightRecordMapper.selectCount(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, today));
        return weightCount > 0;
    }

    /**
     * 检查里程碑，返回本次应发放的积分
     * 只发放当前连续天数对应的里程碑积分（每个里程碑只发放一次）
     */
    private int checkMilestone(UserRank rank, int currentStreak) {
        int points = 0;
        // 里程碑是按顺序的，检查当前连续天数是否刚好达到某个里程碑
        for (var entry : MILESTONE_POINTS.entrySet()) {
            int milestone = entry.getKey();
            if (currentStreak == milestone) {
                points += entry.getValue();
                log.info("用户 {} 达成里程碑: 连续{}天, 奖励{}积分", rank.getUserId(), milestone, entry.getValue());
            }
        }
        return points;
    }

    private int calcRankLevel(int maxStreak) {
        if (maxStreak >= 100) return 7;
        if (maxStreak >= 60) return 6;
        if (maxStreak >= 30) return 5;
        if (maxStreak >= 20) return 4;
        if (maxStreak >= 10) return 3;
        if (maxStreak >= 5) return 2;
        return 1;
    }

    private UserRank getOrCreateRank(Long userId) {
        UserRank rank = userRankMapper.selectOne(
                new LambdaQueryWrapper<UserRank>()
                        .eq(UserRank::getUserId, userId));
        if (rank == null) {
            rank = new UserRank();
            rank.setUserId(userId);
            rank.setPoints(0);
            rank.setRankLevel(1);
            rank.setRankName("觉醒学徒");
            rank.setMaxStreak(0);
            rank.setCurrentStreak(0);
            userRankMapper.insert(rank);
        }
        return rank;
    }

    private RankVO buildRankVO(UserRank rank) {
        RankVO vo = new RankVO();
        vo.setPoints(rank.getPoints());
        vo.setRankLevel(rank.getRankLevel());
        vo.setRankName(rank.getRankName());
        vo.setRankEnName(RANK_CONFIG.get(rank.getRankLevel())[1]);
        vo.setCurrentStreak(rank.getCurrentStreak());
        vo.setMaxStreak(rank.getMaxStreak());
        vo.setDailyPoints(10);

        // 计算下一段位信息
        int nextLevel = rank.getRankLevel() + 1;
        if (nextLevel <= 7) {
            String[] nextConfig = RANK_CONFIG.get(nextLevel);
            vo.setNextRankName(nextConfig[0]);
            vo.setNextRankDays(Integer.parseInt(nextConfig[2]));
        }

        // 填充所有段位配置
        List<RankVO.RankConfigItem> allRanks = new ArrayList<>();
        for (int level = 1; level <= 7; level++) {
            String[] config = RANK_CONFIG.get(level);
            int days = Integer.parseInt(config[2]);
            RankVO.RankConfigItem item = new RankVO.RankConfigItem();
            item.setLevel(level);
            item.setName(config[0]);
            item.setEnName(config[1]);
            item.setRequiredDays(days);
            item.setMilestoneReward(MILESTONE_POINTS.getOrDefault(days, 0));
            item.setIcon(RANK_ICONS.get(level));
            allRanks.add(item);
        }
        vo.setAllRanks(allRanks);

        return vo;
    }
}
