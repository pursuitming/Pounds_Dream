package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.TrainingPlanDTO;
import com.poundsdream.service.TrainingPlanService;
import com.poundsdream.service.TrainingRecommendService;
import com.poundsdream.vo.RecommendedTrainingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "训练计划管理")
@RestController
@RequestMapping("/api/training-plan")
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;
    private final TrainingRecommendService trainingRecommendService;

    @Operation(summary = "创建训练计划")
    @PostMapping
    public Result<Map<String, Object>> createPlan(@Valid @RequestBody TrainingPlanDTO dto) {
        return Result.success(trainingPlanService.createPlan(dto));
    }

    @Operation(summary = "获取训练计划列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getPlans() {
        return Result.success(trainingPlanService.getPlans());
    }

    @Operation(summary = "获取训练计划详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getPlanDetail(@PathVariable Long id) {
        return Result.success(trainingPlanService.getPlanDetail(id));
    }

    @Operation(summary = "更新训练计划")
    @PutMapping("/{id}")
    public Result<Void> updatePlan(@PathVariable Long id, @Valid @RequestBody TrainingPlanDTO dto) {
        trainingPlanService.updatePlan(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除训练计划")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        trainingPlanService.deletePlan(id);
        return Result.success();
    }

    @Operation(summary = "获取今日训练计划")
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayPlan() {
        return Result.success(trainingPlanService.getTodayPlan());
    }

    @Operation(summary = "获取推荐训练计划")
    @GetMapping("/recommend")
    public Result<RecommendedTrainingVO> getRecommendedTraining(
            @RequestParam(required = false, defaultValue = "MEDIUM") String intensity) {
        return Result.success(trainingRecommendService.getRecommendedTraining(intensity));
    }
}
