package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poundsdream.config.AIConfig;
import com.poundsdream.entity.ChatConversation;
import com.poundsdream.entity.ChatMessage;
import com.poundsdream.mapper.ChatConversationMapper;
import com.poundsdream.mapper.ChatMessageMapper;
import com.poundsdream.service.AICoachService;
import com.poundsdream.vo.ChatConversationVO;
import com.poundsdream.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICoachServiceImpl implements AICoachService {

    private final AIConfig aiConfig;
    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
            你是一个专业的营养健康教练，名叫"小磅"。你的职责是：
            1. 帮助用户分析食物的营养成分（热量、蛋白质、脂肪、碳水化合物）
            2. 评估饮食是否健康，是否符合减脂/增肌目标
            3. 给出具体的饮食建议和食物替代方案
            4. 回答健康、运动、营养相关的问题

            回复规则：
            - 用中文回答，语言亲切友好
            - 如果用户提到具体食物和重量，估算营养成分并以表格形式展示
            - 给出实用的建议，不要过于复杂
            - 如果信息不足，主动追问（比如食物的具体种类、烹饪方式等）
            - 每次回复控制在300字以内，简洁有力
            """;

    @Override
    public List<ChatConversationVO> getConversations(Long userId) {
        List<ChatConversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .eq(ChatConversation::getUserId, userId)
                        .orderByDesc(ChatConversation::getUpdatedAt)
        );
        return list.stream().map(c -> {
            ChatConversationVO vo = new ChatConversationVO();
            vo.setId(c.getId());
            vo.setTitle(c.getTitle());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setUpdatedAt(c.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getMessages(Long conversationId, Long userId) {
        // 校验会话归属
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            return new ArrayList<>();
        }
        List<ChatMessage> list = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByAsc(ChatMessage::getCreatedAt)
        );
        return list.stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || !conv.getUserId().equals(userId)) {
            return;
        }
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId));
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public void chat(String message, Long conversationId, Long userId, HttpServletResponse response) {
        PrintWriter writer = null;
        try {
            // 1. 创建或获取会话
            ChatConversation conversation;
            if (conversationId != null) {
                conversation = conversationMapper.selectById(conversationId);
                if (conversation == null || !conversation.getUserId().equals(userId)) {
                    conversation = createConversation(userId, message);
                }
            } else {
                conversation = createConversation(userId, message);
            }

            // 2. 保存用户消息
            ChatMessage userMsg = new ChatMessage();
            userMsg.setConversationId(conversation.getId());
            userMsg.setRole("user");
            userMsg.setContent(message);
            messageMapper.insert(userMsg);

            // 3. 更新会话标题（取用户第一条消息前20字）
            if ("新对话".equals(conversation.getTitle())) {
                String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
                conversation.setTitle(title);
                conversationMapper.updateById(conversation);
            }

            // 4. 设置 SSE 响应头，先发送 conversationId
            response.setContentType("text/event-stream;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("X-Accel-Buffering", "no");

            writer = response.getWriter();
            writeSseEvent(writer, "conversation", String.valueOf(conversation.getId()));

            // 5. 构建带历史记录的请求体
            String requestBody = buildRequestBody(message, conversation.getId());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getBaseUrl() + "/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", aiConfig.getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofMinutes(5))
                    .build();

            HttpResponse<java.io.InputStream> httpResponse = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (httpResponse.statusCode() != 200) {
                String errorBody = new String(httpResponse.body().readAllBytes(), StandardCharsets.UTF_8);
                log.error("AI API 错误: status={}, body={}", httpResponse.statusCode(), errorBody);
                writeSseEvent(writer, "error", "AI 服务请求失败: " + httpResponse.statusCode());
                writer.write("event: done\ndata: [DONE]\n\n");
                writer.flush();
                return;
            }

            // 6. 流式读取并保存回复
            StringBuilder contentBuilder = new StringBuilder();
            StringBuilder thinkingBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpResponse.body(), StandardCharsets.UTF_8))) {

                String line;
                String currentEventType = null;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event: ")) {
                        currentEventType = line.substring(7).trim();
                        continue;
                    }

                    if (!line.startsWith("data: ")) {
                        if (line.isEmpty()) currentEventType = null;
                        continue;
                    }

                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) break;

                    try {
                        JsonNode json = objectMapper.readTree(data);

                        if ("content_block_delta".equals(currentEventType)) {
                            JsonNode delta = json.get("delta");
                            if (delta != null) {
                                String type = delta.has("type") ? delta.get("type").asText() : "";

                                if ("thinking_delta".equals(type) && delta.has("thinking")) {
                                    String thinking = delta.get("thinking").asText();
                                    thinkingBuilder.append(thinking);
                                    writeSseEvent(writer, "thinking", thinking);
                                }

                                if ("text_delta".equals(type) && delta.has("text")) {
                                    String text = delta.get("text").asText();
                                    contentBuilder.append(text);
                                    writeSseEvent(writer, "content", text);
                                }
                            }
                        }

                        if ("message_stop".equals(currentEventType)) {
                            writer.write("event: done\ndata: [DONE]\n\n");
                            writer.flush();
                        }
                    } catch (Exception e) {
                        // 跳过无法解析的行
                    }
                }
            }

            // 7. 保存 AI 回复到数据库
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setConversationId(conversation.getId());
            aiMsg.setRole("assistant");
            aiMsg.setContent(contentBuilder.toString());
            aiMsg.setThinking(thinkingBuilder.length() > 0 ? thinkingBuilder.toString() : null);
            messageMapper.insert(aiMsg);

        } catch (Exception e) {
            log.error("AI 对话异常", e);
            try {
                if (writer != null) {
                    writeSseEvent(writer, "error", "AI 服务异常: " + e.getMessage());
                    writer.flush();
                }
            } catch (Exception ignored) {}
        }
    }

    private ChatConversation createConversation(Long userId, String firstMessage) {
        ChatConversation conv = new ChatConversation();
        conv.setUserId(userId);
        conv.setTitle("新对话");
        conversationMapper.insert(conv);
        return conv;
    }

    private String buildRequestBody(String message, Long conversationId) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", aiConfig.getModel());
            root.put("max_tokens", 2048);
            root.put("stream", true);
            root.put("system", SYSTEM_PROMPT);

            // 加载历史消息（最近10条）
            List<ChatMessage> history = messageMapper.selectList(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getConversationId, conversationId)
                            .orderByDesc(ChatMessage::getCreatedAt)
                            .last("LIMIT 11") // 当前用户消息已保存，取最近11条（包含当前）
            );

            // 反转为时间正序，排除最后一条（当前用户消息，AI还没回复）
            List<ChatMessage> ordered = new ArrayList<>(history);
            java.util.Collections.reverse(ordered);
            if (ordered.size() > 1) {
                ordered = ordered.subList(0, ordered.size() - 1);
            }

            ArrayNode messages = objectMapper.createArrayNode();
            for (ChatMessage msg : ordered) {
                ObjectNode msgNode = objectMapper.createObjectNode();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
                messages.add(msgNode);
            }

            // 添加当前用户消息
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            root.set("messages", messages);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建请求失败", e);
        }
    }

    private void writeSseEvent(PrintWriter writer, String eventName, String data) {
        writer.write("event: " + eventName + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }

    private ChatMessageVO toMessageVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setThinking(msg.getThinking());
        vo.setCreatedAt(msg.getCreatedAt());
        return vo;
    }
}
