package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.SleepRecordDTO;
import com.poundsdream.service.SleepService;
import com.poundsdream.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "睡眠管理")
@RestController
@RequestMapping("/api/sleep")
@RequiredArgsConstructor
public class SleepController {

    private final SleepService sleepService;

    @Operation(summary = "添加睡眠记录")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SleepRecordDTO dto) {
        sleepService.addRecord(dto);
        return Result.success();
    }

    @Operation(summary = "删除睡眠记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sleepService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "获取今日睡眠数据")
    @GetMapping("/today")
    public Result<SleepDailyVO> getToday() {
        return Result.success(sleepService.getTodayData());
    }

    @Operation(summary = "更新每日睡眠目标")
    @PutMapping("/goal")
    public Result<Void> updateGoal(@RequestParam Integer goal) {
        sleepService.updateDailyGoal(goal);
        return Result.success();
    }

    @Operation(summary = "获取睡眠趋势数据")
    @GetMapping("/trend")
    public Result<List<SleepTrendVO>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(sleepService.getTrend(days));
    }

    @Operation(summary = "获取作息规律性评分")
    @GetMapping("/regularity")
    public Result<SleepRegularityVO> getRegularity() {
        return Result.success(sleepService.getRegularity());
    }

    @Operation(summary = "获取智能睡眠推荐")
    @GetMapping("/recommendation")
    public Result<SleepRecommendationVO> getRecommendation() {
        return Result.success(sleepService.getRecommendation());
    }

    @Operation(summary = "获取AI睡眠建议")
    @GetMapping("/ai-advice")
    public Result<String> getAiAdvice() {
        return Result.success(sleepService.getAiAdvice());
    }

    @Operation(summary = "AI睡眠建议（SSE流式输出）")
    @GetMapping("/ai-advice-stream")
    public void streamAiAdvice(HttpServletResponse response) {
        sleepService.streamAiAdvice(response);
    }
}
