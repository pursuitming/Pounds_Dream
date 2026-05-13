package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private String role;

    private String content;

    private String thinking;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
