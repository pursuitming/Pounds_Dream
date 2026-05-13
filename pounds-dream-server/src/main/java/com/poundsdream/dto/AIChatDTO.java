package com.poundsdream.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AIChatDTO {

    @NotBlank(message = "消息不能为空")
    private String message;

    private Long conversationId;
}
