package com.poundsdream.service;

import com.poundsdream.vo.ChatConversationVO;
import com.poundsdream.vo.ChatMessageVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface AICoachService {

    void chat(String message, Long conversationId, Long userId, HttpServletResponse response);

    List<ChatConversationVO> getConversations(Long userId);

    List<ChatMessageVO> getMessages(Long conversationId, Long userId);

    void deleteConversation(Long conversationId, Long userId);
}
