package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.entity.Notification;
import com.poundsdream.mapper.NotificationMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public List<Map<String, Object>> getNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt)
                        .last("LIMIT 50"));

        return list.stream().map(n -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("type", n.getType());
            item.put("title", n.getTitle());
            item.put("content", n.getContent());
            item.put("isRead", n.getIsRead());
            item.put("createdAt", n.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markAsRead(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1));
    }
}
