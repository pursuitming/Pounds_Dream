package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String thinking;
    private LocalDateTime createdAt;
}
