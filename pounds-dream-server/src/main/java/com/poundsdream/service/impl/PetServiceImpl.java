package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import com.poundsdream.service.PetService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.PetLogVO;
import com.poundsdream.vo.PetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetLogMapper petLogMapper;
    private final DietRecordMapper dietRecordMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final UserRankMapper userRankMapper;
    private final UserMapper userMapper;
    private final WaterRecordMapper waterRecordMapper;
    private final WaterGoalMapper waterGoalMapper;
    private final SleepRecordMapper sleepRecordMapper;
    private final SleepGoalMapper sleepGoalMapper;

    // 互动冷却: "userId:action" -> 上次互动时间戳
    private final ConcurrentHashMap<String, Long> cooldownMap = new ConcurrentHashMap<>();

    // 互动冷却秒数
    private static final Map<String, Integer> COOLDOWN_SECONDS = Map.ofEntries(
            Map.entry("pet", 30),
            Map.entry("feed", 60),
            Map.entry("play", 120),
            Map.entry("bath", 90),
            Map.entry("sleep", 180),
            Map.entry("dance", 60)
    );

    // 互动增加的幸福值
    private static final Map<String, Integer> INTERACT_HAPPINESS = Map.ofEntries(
            Map.entry("pet", 3),
            Map.entry("feed", 5),
            Map.entry("play", 8),
            Map.entry("bath", 6),
            Map.entry("sleep", 10),
            Map.entry("dance", 5)
    );

    // 进化体系: level -> {name, icon}
    private static final Map<Integer, String[]> EVOLUTION_CONFIG = Map.of(
            1, new String[]{"梦想之蛋", "🥚"},
            2, new String[]{"元气萌芽", "🐣"},
            3, new String[]{"活力小将", "🐥"},
            4, new String[]{"燃脂战士", "🔥"},
            5, new String[]{"塑形达人", "💪"},
            6, new String[]{"巅峰勇者", "⚡"},
            7, new String[]{"自律之王", "👑"}
    );

    // 升级所需幸福值: level -> 所需总幸福值
    private static final Map<Integer, Integer> LEVEL_UP_HAPPINESS = Map.of(
            1, 100,
            2, 300,
            3, 600,
            4, 1000,
            5, 1500,
            6, 2100,
            7, 2800
    );

    @Override
    public PetVO getPetInfo(Long userId) {
        Pet pet = getOrCreatePet(userId);
        return buildPetVO(pet, userId);
    }

    @Override
    public void renamePet(Long userId, String newName) {
        Pet pet = getOrCreatePet(userId);
        if (newName != null && !newName.isBlank() && newName.length() <= 50) {
            pet.setPetName(newName.trim());
            petMapper.updateById(pet);
        }
    }

    @Override
    public void changePetType(Long userId, String petType) {
        if (!List.of("cat", "dog", "rabbit").contains(petType)) {
            throw new IllegalArgumentException("无效的宠物类型");
        }
        Pet pet = getOrCreatePet(userId);
        pet.setPetType(petType);
        petMapper.updateById(pet);
    }

    @Override
    public List<PetLogVO> getPetLogs(Long userId) {
        List<PetLog> logs = petLogMapper.selectList(
                new LambdaQueryWrapper<PetLog>()
                        .eq(PetLog::getUserId, userId)
                        .orderByDesc(PetLog::getCreatedAt)
                        .last("LIMIT 50"));
        return logs.stream().map(log -> {
            PetLogVO vo = new PetLogVO();
            vo.setAction(log.getAction());
            vo.setActionName(getActionName(log.getAction()));
            vo.setPetMood(log.getPetMood());
            vo.setHappiness(log.getHappiness());
            vo.setCreatedAt(log.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    private String getActionName(String action) {
        return switch (action) {
            case "pet" -> "摸摸头";
            case "feed" -> "喂食";
            case "play" -> "玩耍";
            case "bath" -> "洗澡";
            case "sleep" -> "睡觉";
            case "dance" -> "跳舞";
            default -> action;
        };
    }

    @Override
    public PetVO interact(Long userId, String action) {
        if (!COOLDOWN_SECONDS.containsKey(action)) {
            throw new IllegalArgumentException("无效的互动类型");
        }

        String key = userId + ":" + action;
        long now = System.currentTimeMillis();
        Long lastTime = cooldownMap.get(key);

        if (lastTime != null) {
            long elapsed = (now - lastTime) / 1000;
            int cooldown = COOLDOWN_SECONDS.get(action);
            if (elapsed < cooldown) {
                // 还在冷却中，返回当前状态
                Pet pet = getOrCreatePet(userId);
                PetVO vo = buildPetVO(pet, userId);
                vo.setMessage("互动太频繁啦，休息一下吧~ 冷却 " + (cooldown - elapsed) + " 秒");
                return vo;
            }
        }

        // 执行互动
        cooldownMap.put(key, now);

        // 增加幸福值
        int bonus = INTERACT_HAPPINESS.getOrDefault(action, 0);
        Pet pet = getOrCreatePet(userId);

        // 累积总幸福值
        int newTotalHappiness = pet.getTotalHappiness() + bonus;
        pet.setTotalHappiness(newTotalHappiness);

        // 检查是否可以升级
        int currentLevel = pet.getPetLevel();
        int newLevel = currentLevel;
        String levelUpMessage = null;

        // 检查升级条件（最高7级）
        if (currentLevel < 7) {
            int requiredHappiness = LEVEL_UP_HAPPINESS.getOrDefault(currentLevel, Integer.MAX_VALUE);
            if (newTotalHappiness >= requiredHappiness) {
                newLevel = currentLevel + 1;
                pet.setPetLevel(newLevel);
                String[] evolution = EVOLUTION_CONFIG.get(newLevel);
                levelUpMessage = "🎉 恭喜！" + pet.getPetName() + " 进化为 [" + evolution[0] + "] " + evolution[1] + "！";
            }
        }

        // 保存宠物数据
        petMapper.updateById(pet);

        // 计算当日幸福值（用于显示）
        PetVO vo = buildPetVO(pet, userId);

        // 记录互动日志
        PetLog log = new PetLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setPetMood(vo.getMood());
        log.setHappiness(bonus);
        petLogMapper.insert(log);

        // 设置消息
        if (levelUpMessage != null) {
            vo.setMessage(levelUpMessage);
        } else {
            vo.setMessage(getInteractMessage(action));
        }

        return vo;
    }

    @Override
    public PetVO addWaterExp(Long userId) {
        Pet pet = getOrCreatePet(userId);
        LocalDate today = LocalDate.now();

        // 检查今日是否已经因饮水达标加过经验（通过日志判断）
        boolean alreadyRewarded = petLogMapper.selectCount(
                new LambdaQueryWrapper<PetLog>()
                        .eq(PetLog::getUserId, userId)
                        .eq(PetLog::getAction, "water_goal")
                        .ge(PetLog::getCreatedAt, today.atStartOfDay())
                        .lt(PetLog::getCreatedAt, today.plusDays(1).atStartOfDay())) > 0;

        if (alreadyRewarded) {
            PetVO vo = buildPetVO(pet, userId);
            vo.setMessage("今天已经因饮水达标获得过经验啦~");
            return vo;
        }

        // 检查是否达标
        if (!isWaterGoalMet(userId, today)) {
            PetVO vo = buildPetVO(pet, userId);
            vo.setMessage("还没达到今日饮水目标哦，继续加油~");
            return vo;
        }

        // 加经验
        int bonus = 10;
        pet.setTotalHappiness(pet.getTotalHappiness() + bonus);

        // 检查升级
        int currentLevel = pet.getPetLevel();
        String levelUpMessage = null;
        if (currentLevel < 7) {
            int required = LEVEL_UP_HAPPINESS.getOrDefault(currentLevel, Integer.MAX_VALUE);
            if (pet.getTotalHappiness() >= required) {
                pet.setPetLevel(currentLevel + 1);
                String[] evo = EVOLUTION_CONFIG.get(currentLevel + 1);
                levelUpMessage = "恭喜！宠物升级到 Lv." + (currentLevel + 1) + " " + evo[0] + " " + evo[1];
            }
        }

        petMapper.updateById(pet);

        // 记录日志
        PetLog petLog = new PetLog();
        petLog.setUserId(userId);
        petLog.setAction("water_goal");
        petLog.setPetMood(calcMood(userId, today));
        petLog.setHappiness(bonus);
        petLogMapper.insert(petLog);

        PetVO vo = buildPetVO(pet, userId);
        vo.setMessage(levelUpMessage != null ? levelUpMessage : "饮水达标！宠物获得 +" + bonus + " 经验值 💧");
        return vo;
    }

    @Override
    public PetVO addSleepExp(Long userId) {
        Pet pet = getOrCreatePet(userId);
        LocalDate today = LocalDate.now();

        // 检查今日是否已经因睡眠达标加过经验
        boolean alreadyRewarded = petLogMapper.selectCount(
                new LambdaQueryWrapper<PetLog>()
                        .eq(PetLog::getUserId, userId)
                        .eq(PetLog::getAction, "sleep_goal")
                        .ge(PetLog::getCreatedAt, today.atStartOfDay())
                        .lt(PetLog::getCreatedAt, today.plusDays(1).atStartOfDay())) > 0;

        if (alreadyRewarded) {
            PetVO vo = buildPetVO(pet, userId);
            vo.setMessage("今天已经因睡眠达标获得过经验啦~");
            return vo;
        }

        // 检查是否达标
        if (!isSleepGoalMet(userId, today)) {
            PetVO vo = buildPetVO(pet, userId);
            vo.setMessage("还没达到今日睡眠目标哦，继续加油~");
            return vo;
        }

        // 加经验
        int bonus = 10;
        pet.setTotalHappiness(pet.getTotalHappiness() + bonus);

        // 检查升级
        int currentLevel = pet.getPetLevel();
        String levelUpMessage = null;
        if (currentLevel < 7) {
            int required = LEVEL_UP_HAPPINESS.getOrDefault(currentLevel, Integer.MAX_VALUE);
            if (pet.getTotalHappiness() >= required) {
                pet.setPetLevel(currentLevel + 1);
                String[] evo = EVOLUTION_CONFIG.get(currentLevel + 1);
                levelUpMessage = "恭喜！宠物升级到 Lv." + (currentLevel + 1) + " " + evo[0] + " " + evo[1];
            }
        }

        petMapper.updateById(pet);

        // 记录日志
        PetLog petLog = new PetLog();
        petLog.setUserId(userId);
        petLog.setAction("sleep_goal");
        petLog.setPetMood(calcMood(userId, today));
        petLog.setHappiness(bonus);
        petLogMapper.insert(petLog);

        PetVO vo = buildPetVO(pet, userId);
        vo.setMessage(levelUpMessage != null ? levelUpMessage : "睡眠达标！宠物获得 +" + bonus + " 经验值 😴");
        return vo;
    }

    private Pet getOrCreatePet(Long userId) {
        Pet pet = petMapper.selectOne(
                new LambdaQueryWrapper<Pet>()
                        .eq(Pet::getUserId, userId));
        if (pet == null) {
            pet = new Pet();
            pet.setUserId(userId);
            pet.setPetName("小梦");
            pet.setPetType("cat");
            pet.setPetLevel(1);
            pet.setTotalHappiness(0);
            petMapper.insert(pet);
        }
        // 确保旧数据也有默认值
        if (pet.getPetLevel() == null) {
            pet.setPetLevel(1);
        }
        if (pet.getTotalHappiness() == null) {
            pet.setTotalHappiness(0);
        }
        return pet;
    }

    private PetVO buildPetVO(Pet pet, Long userId) {
        PetVO vo = new PetVO();
        vo.setPetName(pet.getPetName());
        vo.setPetType(pet.getPetType() != null ? pet.getPetType() : "cat");

        LocalDate today = LocalDate.now();

        // 计算心情
        String mood = calcMood(userId, today);
        vo.setMood(mood);

        // 计算当日幸福值（用于显示）
        int dailyHappiness = calcHappiness(userId, today);
        vo.setHappiness(dailyHappiness);

        // 设置宠物等级和累积幸福值
        int petLevel = pet.getPetLevel() != null ? pet.getPetLevel() : 1;
        int totalHappiness = pet.getTotalHappiness() != null ? pet.getTotalHappiness() : 0;
        vo.setPetLevel(petLevel);
        vo.setTotalHappiness(totalHappiness);

        // 获取段位信息
        UserRank rank = userRankMapper.selectOne(
                new LambdaQueryWrapper<UserRank>()
                        .eq(UserRank::getUserId, userId));
        int currentStreak = rank != null ? rank.getCurrentStreak() : 0;
        vo.setCurrentStreak(currentStreak);

        // 使用宠物等级计算进化形态（而非段位等级）
        String[] evolution = EVOLUTION_CONFIG.getOrDefault(petLevel, EVOLUTION_CONFIG.get(1));
        vo.setEvolutionLevel(petLevel);
        vo.setEvolutionName(evolution[0]);
        vo.setEvolutionIcon(evolution[1]);

        // 计算距离上次记录的天数
        int daysSince = calcDaysSinceLastRecord(userId, today);
        vo.setDaysSinceLastRecord(daysSince);

        // 宠物台词
        vo.setMessage(getPetMessage(mood, petLevel, daysSince));

        // 计算冷却
        vo.setCooldowns(getCooldowns(userId));

        return vo;
    }

    private String calcMood(Long userId, LocalDate today) {
        boolean hasDiet = dietRecordMapper.selectCount(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, today)) > 0;

        boolean hasExercise = exerciseRecordMapper.selectCount(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, today)) > 0;

        // 今天有运动+饮食 → HAPPY
        if (hasDiet && hasExercise) {
            return "HAPPY";
        }

        // 检查连续打卡
        UserRank rank = userRankMapper.selectOne(
                new LambdaQueryWrapper<UserRank>()
                        .eq(UserRank::getUserId, userId));
        if (rank != null && rank.getCurrentStreak() != null && rank.getCurrentStreak() >= 3) {
            return "ENERGETIC";
        }

        // 检查最近记录情况
        int daysSince = calcDaysSinceLastRecord(userId, today);
        if (daysSince >= 3) {
            return "MISSING";
        }
        if (daysSince >= 1) {
            return "SLEEPY";
        }

        return "NORMAL";
    }

    private int calcHappiness(Long userId, LocalDate today) {
        int score = 0;

        // 今日有饮食记录: +20
        boolean hasDiet = dietRecordMapper.selectCount(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, today)) > 0;
        if (hasDiet) score += 20;

        // 今日有运动记录: +20
        boolean hasExercise = exerciseRecordMapper.selectCount(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, today)) > 0;
        if (hasExercise) score += 20;

        // 今日有体重记录: +10
        boolean hasWeight = weightRecordMapper.selectCount(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, today)) > 0;
        if (hasWeight) score += 10;

        // BMI在正常范围: +10
        WeightRecord todayWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, today));
        if (todayWeight != null) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getHeight() != null) {
                BigDecimal bmi = CalorieCalculator.calcBMI(todayWeight.getWeight(), user.getHeight());
                if (bmi != null) {
                    double bmiVal = bmi.doubleValue();
                    if (bmiVal >= 18.5 && bmiVal <= 24.9) {
                        score += 10;
                    }
                }
            }
        }

        // 今日饮水达标: +10
        boolean waterMet = isWaterGoalMet(userId, today);
        if (waterMet) score += 10;

        // 今日睡眠达标: +10
        boolean sleepMet = isSleepGoalMet(userId, today);
        if (sleepMet) score += 10;

        // 今日互动增加的幸福值（最高40）
        int interactHappiness = calcTodayInteractHappiness(userId, today);
        score += Math.min(40, interactHappiness);

        return Math.max(0, Math.min(100, score));
    }

    private boolean isWaterGoalMet(Long userId, LocalDate today) {
        int todayAmount = waterRecordMapper.sumAmountByDate(userId, today);
        if (todayAmount <= 0) return false;

        // 获取目标
        WaterGoal waterGoal = waterGoalMapper.selectOne(
                new LambdaQueryWrapper<WaterGoal>().eq(WaterGoal::getUserId, userId));
        int goal = 2000;
        if (waterGoal != null) {
            goal = waterGoal.getDailyGoal();
        } else {
            WeightRecord latestWeight = weightRecordMapper.selectOne(
                    new LambdaQueryWrapper<WeightRecord>()
                            .eq(WeightRecord::getUserId, userId)
                            .orderByDesc(WeightRecord::getRecordDate)
                            .last("LIMIT 1"));
            if (latestWeight != null && latestWeight.getWeight() != null) {
                goal = latestWeight.getWeight().multiply(BigDecimal.valueOf(35)).intValue();
            }
        }
        return todayAmount >= goal;
    }

    private boolean isSleepGoalMet(Long userId, LocalDate today) {
        // 查找最近的主睡眠记录
        SleepRecord lastSleep = sleepRecordMapper.selectOne(
                new LambdaQueryWrapper<SleepRecord>()
                        .eq(SleepRecord::getUserId, userId)
                        .eq(SleepRecord::getIsNap, 0)
                        .orderByDesc(SleepRecord::getRecordDate)
                        .last("LIMIT 1"));
        if (lastSleep == null || lastSleep.getDuration() <= 0) return false;

        // 获取目标
        SleepGoal sleepGoal = sleepGoalMapper.selectOne(
                new LambdaQueryWrapper<SleepGoal>().eq(SleepGoal::getUserId, userId));
        int goal = sleepGoal != null ? sleepGoal.getDailyGoal() : 480;

        return lastSleep.getDuration() >= goal;
    }

    private int calcTodayInteractHappiness(Long userId, LocalDate today) {
        // 查询今日的互动日志，计算总互动幸福值
        List<PetLog> todayLogs = petLogMapper.selectList(
                new LambdaQueryWrapper<PetLog>()
                        .eq(PetLog::getUserId, userId)
                        .ge(PetLog::getCreatedAt, today.atStartOfDay())
                        .lt(PetLog::getCreatedAt, today.plusDays(1).atStartOfDay()));

        int totalInteractHappiness = 0;
        for (PetLog log : todayLogs) {
            totalInteractHappiness += log.getHappiness();
        }
        return totalInteractHappiness;
    }

    private int calcDaysSinceLastRecord(Long userId, LocalDate today) {
        LocalDate lastDate = null;

        DietRecord lastDiet = dietRecordMapper.selectOne(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .orderByDesc(DietRecord::getRecordDate)
                        .last("LIMIT 1"));
        if (lastDiet != null) lastDate = lastDiet.getRecordDate();

        ExerciseRecord lastExercise = exerciseRecordMapper.selectOne(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .orderByDesc(ExerciseRecord::getRecordDate)
                        .last("LIMIT 1"));
        if (lastExercise != null && (lastDate == null || lastExercise.getRecordDate().isAfter(lastDate))) {
            lastDate = lastExercise.getRecordDate();
        }

        WeightRecord lastWeight = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .orderByDesc(WeightRecord::getRecordDate)
                        .last("LIMIT 1"));
        if (lastWeight != null && (lastDate == null || lastWeight.getRecordDate().isAfter(lastDate))) {
            lastDate = lastWeight.getRecordDate();
        }

        if (lastDate == null) return 99;
        return (int) (today.toEpochDay() - lastDate.toEpochDay());
    }

    private String getPetMessage(String mood, int level, int daysSince) {
        return switch (mood) {
            case "HAPPY" -> randomPick(
                    "今天状态超棒！继续保持哦~ 💪",
                    "吃了也动了，完美的一天！",
                    "我今天特别开心，因为你很自律！"
            );
            case "ENERGETIC" -> randomPick(
                    "连续打卡" + "太厉害了！我已经闪闪发光了 ✨",
                    "你就是我的能量来源！继续冲！",
                    "这种坚持，注定不平凡！"
            );
            case "SLEEPY" -> randomPick(
                    "好困...你是不是忘记我了？😢",
                    "已经" + daysSince + "天没记录了，我想你了~",
                    "快来记录一下吧，我会很开心的！"
            );
            case "MISSING" -> randomPick(
                    "你已经" + daysSince + "天没来了...我一直在等你 🥺",
                    "别丢下我一个人嘛，回来记录一下吧~",
                    "我的能量快耗尽了，需要你的关爱..."
            );
            default -> {
                if (level >= 5) yield randomPick(
                        "今天的我超帅的！要不要一起运动？",
                        "作为" + EVOLUTION_CONFIG.get(level)[0] + "，我很有信心！",
                        "我们的旅程才刚刚开始~"
                );
                yield randomPick(
                        "今天也要加油哦！记录一下饮食吧~",
                        "我在等你来打卡呢！",
                        "一起变更好吧！"
                );
            }
        };
    }

    private String randomPick(String... messages) {
        return messages[ThreadLocalRandom.current().nextInt(messages.length)];
    }

    private String getInteractMessage(String action) {
        return switch (action) {
            case "pet" -> randomPick(
                    "喵~ 好舒服，再摸摸我嘛~ ❤️",
                    "咕噜咕噜~ 我最喜欢你了！",
                    "被摸头的感觉真好~ 继续继续！"
            );
            case "feed" -> randomPick(
                    "好吃好吃！谢谢你~ 🍎",
                    "嗯~ 美味！能量满满！",
                    "饱饱的，好幸福~ 还要还要！"
            );
            case "play" -> randomPick(
                    "哇！好好玩！再来一次！⚡",
                    "和你玩耍是最开心的事！",
                    "哈哈哈~ 我跳我跳我跳跳跳！"
            );
            case "bath" -> randomPick(
                    "泡泡澡好舒服~ 浑身香喷喷的！🫧",
                    "洗完澡感觉焕然一新！",
                    "搓搓搓~ 干干净净！"
            );
            case "sleep" -> randomPick(
                    "呼噜噜~ 做了个好梦...🌙",
                    "睡饱了精力充沛！",
                    "午觉最舒服了~"
            );
            case "dance" -> randomPick(
                    "跟着节奏摇摆~ 🎵",
                    "左扭扭右扭扭~ 好开心！",
                    "跳舞使我快乐！"
            );
            default -> "嗯？";
        };
    }

    private Map<String, Integer> getCooldowns(Long userId) {
        Map<String, Integer> result = new HashMap<>();
        long now = System.currentTimeMillis();
        for (var entry : COOLDOWN_SECONDS.entrySet()) {
            String action = entry.getKey();
            int cooldown = entry.getValue();
            String key = userId + ":" + action;
            Long lastTime = cooldownMap.get(key);
            if (lastTime != null) {
                long elapsed = (now - lastTime) / 1000;
                if (elapsed < cooldown) {
                    result.put(action, (int) (cooldown - elapsed));
                } else {
                    result.put(action, 0);
                }
            } else {
                result.put(action, 0);
            }
        }
        return result;
    }
}
