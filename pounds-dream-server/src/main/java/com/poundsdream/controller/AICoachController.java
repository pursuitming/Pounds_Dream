package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.AIChatDTO;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.AICoachService;
import com.poundsdream.vo.ChatConversationVO;
import com.poundsdream.vo.ChatMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI健康教练")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AICoachController {

    private final AICoachService aiCoachService;

    @Operation(summary = "AI对话（SSE流式输出）")
    @PostMapping("/chat")
    public void chat(@Valid @RequestBody AIChatDTO dto, HttpServletResponse response) {
        Long userId = SecurityUtil.getCurrentUserId();
        aiCoachService.chat(dto.getMessage(), dto.getConversationId(), userId, response);
    }

    @Operation(summary = "获取对话列表")
    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> getConversations() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiCoachService.getConversations(userId));
    }

    @Operation(summary = "获取对话消息")
    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageVO>> getMessages(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(aiCoachService.getMessages(id, userId));
    }

    @Operation(summary = "删除对话")
    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        aiCoachService.deleteConversation(id, userId);
        return Result.success();
    }
}
