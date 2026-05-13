package com.poundsdream.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.entity.*;
import com.poundsdream.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MealReminderTask {

    private final MealStatusMapper mealStatusMapper;
    private final NotificationMapper notificationMapper;
    private final DietRecordMapper dietRecordMapper;
    private final UserMapper userMapper;

    // 截止时间配置
    private static final int BREAKFAST_DEADLINE_HOUR = 10;
    private static final int LUNCH_DEADLINE_HOUR = 14;
    private static final int DINNER_DEADLINE_HOUR = 21;

    @Scheduled(cron = "0 */30 * * * ?")
    public void checkMissedMeals() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));

        for (User user : users) {
            try {
                checkUserMeals(user.getId(), today, now);
            } catch (Exception e) {
                log.error("用户 {} 漏餐检查失败", user.getId(), e);
            }
        }
    }

    private void checkUserMeals(Long userId, LocalDate today, LocalTime now) {
        checkMealDeadline(userId, today, 1, BREAKFAST_DEADLINE_HOUR, now, "早餐");
        checkMealDeadline(userId, today, 2, LUNCH_DEADLINE_HOUR, now, "午餐");
        checkMealDeadline(userId, today, 3, DINNER_DEADLINE_HOUR, now, "晚餐");
    }

    private void checkMealDeadline(Long userId, LocalDate date, int mealType, int deadlineHour, LocalTime now, String mealName) {
        if (now.getHour() < deadlineHour) return;

        // 检查是否已有该餐次的状态记录
        MealStatus status = mealStatusMapper.selectOne(
                new LambdaQueryWrapper<MealStatus>()
                        .eq(MealStatus::getUserId, userId)
                        .eq(MealStatus::getMealDate, date)
                        .eq(MealStatus::getMealType, mealType));

        // 已经记录/跳过/已提醒的不再处理
        if (status != null && !"PENDING".equals(status.getStatus())) return;

        // 检查是否有饮食记录
        Long dietCount = dietRecordMapper.selectCount(
                new LambdaQueryWrapper<DietRecord>()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date)
                        .eq(DietRecord::getMealType, mealType));

        if (dietCount > 0) {
            // 已记录，更新状态
            if (status == null) {
                status = new MealStatus();
                status.setUserId(userId);
                status.setMealDate(date);
                status.setMealType(mealType);
            }
            status.setStatus("RECORDED");
            saveMealStatus(status);
            return;
        }

        // 超时未记录，标记为EXPIRED并发送通知
        if (status == null) {
            status = new MealStatus();
            status.setUserId(userId);
            status.setMealDate(date);
            status.setMealType(mealType);
        }
        status.setStatus("EXPIRED");
        saveMealStatus(status);

        // 检查是否已发送过通知
        Long existingNotif = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getType, "MISSED_MEAL")
                        .likeRight(Notification::getContent, mealName + "|" + date));

        if (existingNotif == 0) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType("MISSED_MEAL");
            notification.setTitle("漏餐提醒");
            notification.setContent(mealName + "|" + date + " 您还没有记录" + mealName + "，记得补充饮食记录哦！");
            notification.setIsRead(0);
            notificationMapper.insert(notification);
            log.info("发送漏餐提醒: userId={}, meal={}, date={}", userId, mealName, date);
        }
    }

    private void saveMealStatus(MealStatus status) {
        if (status.getId() != null) {
            mealStatusMapper.updateById(status);
        } else {
            mealStatusMapper.insert(status);
        }
    }
}
