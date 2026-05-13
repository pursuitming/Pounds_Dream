package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poundsdream.config.AIConfig;
import com.poundsdream.dto.SleepRecordDTO;
import com.poundsdream.entity.SleepGoal;
import com.poundsdream.entity.SleepRecord;
import com.poundsdream.mapper.SleepRecordMapper;
import com.poundsdream.mapper.SleepGoalMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.SleepService;
import com.poundsdream.vo.*;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SleepServiceImpl implements SleepService {

    private final SleepRecordMapper sleepRecordMapper;
    private final SleepGoalMapper sleepGoalMapper;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addRecord(SleepRecordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        int duration = (int) ChronoUnit.MINUTES.between(dto.getBedTime(), dto.getWakeTime());
        if (duration <= 0) {
            throw new RuntimeException("起床时间必须晚于上床时间");
        }

        SleepRecord record = new SleepRecord();
        record.setUserId(userId);
        record.setBedTime(dto.getBedTime());
        record.setWakeTime(dto.getWakeTime());
        record.setDuration(duration);
        record.setQuality(dto.getQuality() != null ? dto.getQuality() : 3);
        record.setTags(dto.getTags() != null ? dto.getTags() : "[]");
        record.setIsNap(dto.getIsNap() != null && dto.getIsNap() ? 1 : 0);
        record.setRecordDate(dto.getWakeTime().toLocalDate());
        record.setNote(dto.getNote() != null ? dto.getNote() : "");

        sleepRecordMapper.insert(record);
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        SleepRecord record = sleepRecordMapper.selectById(id);
        if (record != null && record.getUserId().equals(userId)) {
            sleepRecordMapper.deleteById(id);
        }
    }

    @Override
    public SleepDailyVO getTodayData() {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        SleepRecord lastSleep = sleepRecordMapper.selectOne(
                new LambdaQueryWrapper<SleepRecord>()
                        .eq(SleepRecord::getUserId, userId)
                        .eq(SleepRecord::getIsNap, 0)
                        .orderByDesc(SleepRecord::getRecordDate)
                        .last("LIMIT 1"));

        List<SleepRecord> records = sleepRecordMapper.selectList(
                new LambdaQueryWrapper<SleepRecord>()
                        .eq(SleepRecord::getUserId, userId)
                        .in(SleepRecord::getRecordDate, today, yesterday)
                        .orderByDesc(SleepRecord::getBedTime));

        List<SleepRecordVO> recordVOs = records.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        int dailyGoal = getDailyGoal(userId);

        LocalDate startDate = today.minusDays(6);
        List<Map<String, Object>> weekData = sleepRecordMapper.statByDateRange(userId, startDate, today);

        Map<String, Map<String, Object>> dateStatMap = new HashMap<>();
        for (Map<String, Object> row : weekData) {
            dateStatMap.put(row.get("recordDate").toString(), row);
        }

        List<SleepDayStat> recentDays = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> stat = dateStatMap.get(date.toString());
            if (stat != null) {
                recentDays.add(SleepDayStat.builder()
                        .date(date)
                        .totalDuration(((Number) stat.get("totalDuration")).intValue())
                        .avgQuality(((Number) stat.get("avgQuality")).doubleValue())
                        .build());
            } else {
                recentDays.add(SleepDayStat.builder()
                        .date(date)
                        .totalDuration(0)
                        .avgQuality(0.0)
                        .build());
            }
        }

        int lastDuration = lastSleep != null ? lastSleep.getDuration() : 0;
        int lastQuality = lastSleep != null ? lastSleep.getQuality() : 0;
        int score = calcScore(lastDuration, dailyGoal, lastQuality);
        int streak = calcStreak(userId);

        int percentage = dailyGoal > 0 ? Math.min(100, Math.round((float) lastDuration / dailyGoal * 100)) : 0;

        return SleepDailyVO.builder()
                .lastDuration(lastDuration)
                .lastQuality(lastQuality)
                .lastTags(lastSleep != null ? lastSleep.getTags() : "[]")
                .dailyGoal(dailyGoal)
                .percentage(percentage)
                .score(score)
                .streak(streak)
                .records(recordVOs)
                .recentDays(recentDays)
                .build();
    }

    @Override
    public void updateDailyGoal(Integer goal) {
        Long userId = SecurityUtil.getCurrentUserId();

        SleepGoal sleepGoal = sleepGoalMapper.selectOne(
                new LambdaQueryWrapper<SleepGoal>().eq(SleepGoal::getUserId, userId));

        if (sleepGoal == null) {
            sleepGoal = new SleepGoal();
            sleepGoal.setUserId(userId);
            sleepGoal.setDailyGoal(goal);
            sleepGoalMapper.insert(sleepGoal);
        } else {
            sleepGoal.setDailyGoal(goal);
            sleepGoalMapper.updateById(sleepGoal);
        }
    }

    @Override
    public List<SleepTrendVO> getTrend(int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Map<String, Object>> data = sleepRecordMapper.statByDateRange(userId, startDate, today);

        Map<String, Map<String, Object>> dateStatMap = new HashMap<>();
        for (Map<String, Object> row : data) {
            dateStatMap.put(row.get("recordDate").toString(), row);
        }

        List<SleepTrendVO> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> stat = dateStatMap.get(date.toString());
            if (stat != null) {
                trend.add(SleepTrendVO.builder()
                        .date(date)
                        .totalDuration(((Number) stat.get("totalDuration")).intValue())
                        .avgQuality(((Number) stat.get("avgQuality")).doubleValue())
                        .build());
            } else {
                trend.add(SleepTrendVO.builder()
                        .date(date)
                        .totalDuration(0)
                        .avgQuality(0.0)
                        .build());
            }
        }
        return trend;
    }

    @Override
    public SleepRegularityVO getRegularity() {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        List<Map<String, Object>> bedWakeData = sleepRecordMapper.selectBedWakeTimes(userId, startDate, today);

        if (bedWakeData.isEmpty()) {
            return SleepRegularityVO.builder()
                    .score(0)
                    .avgBedTime("--:--")
                    .avgWakeTime("--:--")
                    .bedTimeStdDev(0)
                    .wakeTimeStdDev(0)
                    .build();
        }

        List<Integer> bedMinutes = new ArrayList<>();
        List<Integer> wakeMinutes = new ArrayList<>();

        for (Map<String, Object> row : bedWakeData) {
            Object bedTimeObj = row.get("bed_time");
            Object wakeTimeObj = row.get("wake_time");

            if (bedTimeObj != null && wakeTimeObj != null) {
                LocalDateTime bedTime = parseDateTime(bedTimeObj);
                LocalDateTime wakeTime = parseDateTime(wakeTimeObj);

                if (bedTime != null && wakeTime != null) {
                    bedMinutes.add(bedTime.getHour() * 60 + bedTime.getMinute());
                    int wakeMin = wakeTime.getHour() * 60 + wakeTime.getMinute();
                    wakeMinutes.add(wakeMin);
                }
            }
        }

        if (bedMinutes.isEmpty()) {
            return SleepRegularityVO.builder()
                    .score(0)
                    .avgBedTime("--:--")
                    .avgWakeTime("--:--")
                    .bedTimeStdDev(0)
                    .wakeTimeStdDev(0)
                    .build();
        }

        // 处理跨天的上床时间（如23:30算作-30而不是1410）
        List<Integer> adjustedBed = adjustCrossDayMinutes(bedMinutes);
        int avgBed = (int) adjustedBed.stream().mapToInt(Integer::intValue).average().orElse(0);
        int avgWake = (int) wakeMinutes.stream().mapToInt(Integer::intValue).average().orElse(0);
        int bedStdDev = (int) calcStdDev(adjustedBed);
        int wakeStdDev = (int) calcStdDev(wakeMinutes);

        int score = calcRegularityScore(bedStdDev, wakeStdDev);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        String avgBedStr = formatMinutes(avgBed, fmt);
        String avgWakeStr = formatMinutes(avgWake, fmt);

        return SleepRegularityVO.builder()
                .score(score)
                .avgBedTime(avgBedStr)
                .avgWakeTime(avgWakeStr)
                .bedTimeStdDev(bedStdDev)
                .wakeTimeStdDev(wakeStdDev)
                .build();
    }

    @Override
    public SleepRecommendationVO getRecommendation() {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();

        Integer exerciseDuration = sleepRecordMapper.sumExerciseDurationByDate(userId, today);
        if (exerciseDuration == null) exerciseDuration = 0;

        String exerciseLevel;
        int recommendedDuration;
        String reason;

        if (exerciseDuration == 0) {
            exerciseLevel = "无运动";
            recommendedDuration = 480;
            reason = "今天没有运动记录，建议保持 8 小时基础睡眠，帮助身体恢复。";
        } else if (exerciseDuration <= 30) {
            exerciseLevel = "轻度运动";
            recommendedDuration = 480;
            reason = String.format("今天运动了 %d 分钟，属于轻度运动。建议保持 8 小时睡眠。", exerciseDuration);
        } else if (exerciseDuration <= 60) {
            exerciseLevel = "中等运动";
            recommendedDuration = 510;
            reason = String.format("今天运动了 %d 分钟，运动量适中。建议延长至 8.5 小时，帮助肌肉恢复。", exerciseDuration);
        } else {
            exerciseLevel = "高强度运动";
            recommendedDuration = 540;
            reason = String.format("今天运动了 %d 分钟，运动量较大。建议 9 小时睡眠，促进深度恢复和肌肉修复。", exerciseDuration);
        }

        return SleepRecommendationVO.builder()
                .recommendedDuration(recommendedDuration)
                .exerciseDuration(exerciseDuration)
                .exerciseLevel(exerciseLevel)
                .reason(reason)
                .build();
    }

    @Override
    public String getAiAdvice() {
        String prompt = buildAdvicePrompt();
        try {
            return callAi(prompt);
        } catch (Exception e) {
            log.error("获取AI睡眠建议失败", e);
            return "暂时无法获取AI建议，请稍后再试。保持规律作息是改善睡眠的关键！";
        }
    }

    @Override
    public void streamAiAdvice(HttpServletResponse response) {
        String prompt = buildAdvicePrompt();
        log.info("AI睡眠建议 - 开始流式输出, prompt长度: {}", prompt.length());

        PrintWriter writer = null;
        try {
            response.setContentType("text/event-stream;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("X-Accel-Buffering", "no");

            writer = response.getWriter();

            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", aiConfig.getModel());
            root.put("max_tokens", 512);
            root.put("stream", true);

            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            ArrayNode messages = objectMapper.createArrayNode();
            messages.add(userMsg);
            root.set("messages", messages);

            root.put("system", "你是一个专业的睡眠健康顾问，用中文给出简洁实用的建议。");

            String requestBody = objectMapper.writeValueAsString(root);
            log.info("AI睡眠建议 - 请求URL: {}", aiConfig.getBaseUrl() + "/v1/messages");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getBaseUrl() + "/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", aiConfig.getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofMinutes(2))
                    .build();

            HttpResponse<java.io.InputStream> httpResponse = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            log.info("AI睡眠建议 - 响应状态码: {}", httpResponse.statusCode());

            if (httpResponse.statusCode() != 200) {
                String errorBody = new String(httpResponse.body().readAllBytes(), StandardCharsets.UTF_8);
                log.error("AI API 错误: status={}, body={}", httpResponse.statusCode(), errorBody);
                writeSseEvent(writer, "error", "AI 服务请求失败: " + httpResponse.statusCode());
                writer.write("event: done\ndata: [DONE]\n\n");
                writer.flush();
                return;
            }

            boolean hasContent = false;
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

                    try {
                        JsonNode json = objectMapper.readTree(data);

                        if ("content_block_delta".equals(currentEventType)) {
                            JsonNode delta = json.get("delta");
                            if (delta != null) {
                                String type = delta.has("type") ? delta.get("type").asText() : "";

                                if ("thinking_delta".equals(type) && delta.has("thinking")) {
                                    String thinking = delta.get("thinking").asText();
                                    hasContent = true;
                                    writeSseEvent(writer, "thinking", thinking);
                                }
                                if ("text_delta".equals(type) && delta.has("text")) {
                                    String text = delta.get("text").asText();
                                    hasContent = true;
                                    writeSseEvent(writer, "content", text);
                                }
                            }
                        }

                        if ("message_stop".equals(currentEventType)) {
                            log.info("AI睡眠建议 - 流式输出完成, hasContent: {}", hasContent);
                            writer.write("event: done\ndata: [DONE]\n\n");
                            writer.flush();
                            return;
                        }
                    } catch (Exception parseEx) {
                        log.warn("AI SSE 解析跳过: {}", line);
                    }
                }
            }

            if (!hasContent) {
                log.warn("AI睡眠建议 - 未收到任何可输出内容");
                writeSseEvent(writer, "content", "暂时无法生成建议，请稍后再试。");
            }
            writer.write("event: done\ndata: [DONE]\n\n");
            writer.flush();

        } catch (Exception e) {
            log.error("AI睡眠建议流式输出异常", e);
            try {
                if (writer != null) {
                    writeSseEvent(writer, "error", "AI 服务异常: " + e.getMessage());
                    writer.write("event: done\ndata: [DONE]\n\n");
                    writer.flush();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private String buildAdvicePrompt() {
        SleepDailyVO todayData = getTodayData();
        SleepRegularityVO regularity = getRegularity();
        List<SleepTrendVO> trend = getTrend(7);

        int avgDuration = trend.stream()
                .mapToInt(SleepTrendVO::getTotalDuration)
                .filter(d -> d > 0)
                .average()
                .orElse(0) > 0
                ? (int) trend.stream().mapToInt(SleepTrendVO::getTotalDuration).filter(d -> d > 0).average().orElse(0)
                : 0;

        double avgQuality = trend.stream()
                .mapToDouble(SleepTrendVO::getAvgQuality)
                .filter(q -> q > 0)
                .average()
                .orElse(0);

        long daysWithRecords = trend.stream()
                .filter(d -> d.getTotalDuration() > 0)
                .count();

        return String.format("""
                你是专业睡眠顾问。请根据以下用户睡眠数据，给出3条简短的个性化睡眠改善建议。

                用户数据：
                - 今日睡眠时长：%d分钟（%.1f小时）
                - 今日睡眠质量评分：%d/5
                - 近7天平均时长：%d分钟（%.1f小时）
                - 近7天平均质量：%.1f/5
                - 作息规律性评分：%d/100
                - 平均上床时间：%s
                - 平均起床时间：%s
                - 连续记录天数：%d天
                - 近7天有记录天数：%d天

                要求：
                - 用中文回复
                - 3条建议，每条一句话，简洁实用
                - 如果数据不足（记录天数少），鼓励坚持记录
                - 不要使用markdown格式，直接输出纯文本
                """,
                todayData.getLastDuration(), todayData.getLastDuration() / 60.0,
                todayData.getLastQuality(),
                avgDuration, avgDuration / 60.0,
                avgQuality,
                regularity.getScore(),
                regularity.getAvgBedTime(),
                regularity.getAvgWakeTime(),
                todayData.getStreak(),
                daysWithRecords
        );
    }

    private void writeSseEvent(PrintWriter writer, String eventName, String data) {
        writer.write("event: " + eventName + "\n");
        writer.write("data: " + data + "\n\n");
        writer.flush();
    }

    // ========== 私有方法 ==========

    private int calcStreak(Long userId) {
        List<LocalDate> dates = sleepRecordMapper.selectDistinctDates(userId);
        if (dates.isEmpty()) return 0;

        int streak = 0;
        LocalDate expected = LocalDate.now();

        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (date.isBefore(expected)) {
                break;
            }
        }
        return streak;
    }

    private int getDailyGoal(Long userId) {
        SleepGoal sleepGoal = sleepGoalMapper.selectOne(
                new LambdaQueryWrapper<SleepGoal>().eq(SleepGoal::getUserId, userId));
        return sleepGoal != null ? sleepGoal.getDailyGoal() : 480;
    }

    private int calcScore(int duration, int goal, int quality) {
        if (duration <= 0) return 0;
        double durationRatio = Math.min(1.0, (double) duration / goal);
        int durationScore = (int) (durationRatio * 60);
        int qualityScore = (int) ((quality / 5.0) * 40);
        return Math.min(100, durationScore + qualityScore);
    }

    private int calcRegularityScore(int bedStdDev, int wakeStdDev) {
        int avgStdDev = (bedStdDev + wakeStdDev) / 2;
        if (avgStdDev <= 30) return 90 + Math.max(0, (30 - avgStdDev) / 3);
        if (avgStdDev <= 60) return 70 + (int) ((60 - avgStdDev) / 30.0 * 20);
        if (avgStdDev <= 90) return 50 + (int) ((90 - avgStdDev) / 30.0 * 20);
        return Math.max(10, 50 - (int) ((avgStdDev - 90) / 30.0 * 20));
    }

    private List<Integer> adjustCrossDayMinutes(List<Integer> minutes) {
        List<Integer> adjusted = new ArrayList<>();
        for (int m : minutes) {
            // 如果时间在18:00之后（1080分钟），说明是前一天的晚上，转为负数
            if (m >= 1080) {
                adjusted.add(m - 1440);
            } else {
                adjusted.add(m);
            }
        }
        return adjusted;
    }

    private double calcStdDev(List<Integer> values) {
        if (values.size() < 2) return 0;
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private String formatMinutes(int totalMinutes, DateTimeFormatter fmt) {
        int h = ((totalMinutes % 1440) + 1440) % 1440;
        int hour = h / 60;
        int minute = h % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    private LocalDateTime parseDateTime(Object obj) {
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }
        if (obj instanceof String) {
            return LocalDateTime.parse((String) obj);
        }
        return null;
    }

    private String callAi(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", aiConfig.getModel());
        root.put("max_tokens", 512);
        root.put("stream", false);

        ObjectNode systemMsg = objectMapper.createObjectNode();
        systemMsg.put("role", "user");
        systemMsg.put("content", prompt);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(systemMsg);
        root.set("messages", messages);

        // system prompt
        root.put("system", "你是一个专业的睡眠健康顾问，用中文给出简洁实用的建议。");

        String requestBody = objectMapper.writeValueAsString(root);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiConfig.getBaseUrl() + "/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", aiConfig.getApiKey())
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofMinutes(2))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            log.error("AI API 错误: status={}, body={}", response.statusCode(), response.body());
            throw new RuntimeException("AI 请求失败: " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode content = json.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode block : content) {
                String type = block.has("type") ? block.get("type").asText() : "";
                if ("text".equals(type) && block.has("text")) {
                    return block.get("text").asText();
                }
            }
            // 没有 text 块，尝试取 thinking
            for (JsonNode block : content) {
                String type = block.has("type") ? block.get("type").asText() : "";
                if ("thinking".equals(type) && block.has("thinking")) {
                    return block.get("thinking").asText();
                }
            }
        }
        return "暂无建议";
    }

    private SleepRecordVO toVO(SleepRecord record) {
        return SleepRecordVO.builder()
                .id(record.getId())
                .bedTime(record.getBedTime())
                .wakeTime(record.getWakeTime())
                .duration(record.getDuration())
                .quality(record.getQuality())
                .tags(record.getTags())
                .isNap(record.getIsNap() == 1)
                .note(record.getNote())
                .build();
    }
}
