package com.poundsdream.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    List<Map<String, Object>> getNotifications();

    long getUnreadCount();

    void markAsRead(Long id);

    void markAllAsRead();
}
