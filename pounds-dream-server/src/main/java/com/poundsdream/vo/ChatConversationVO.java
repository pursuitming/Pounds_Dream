package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversationVO {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
