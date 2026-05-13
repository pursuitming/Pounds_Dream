package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getNotifications() {
        return Result.success(notificationService.getNotifications());
    }

    @Operation(summary = "获取未读通知数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @Operation(summary = "标记单条通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }
}
