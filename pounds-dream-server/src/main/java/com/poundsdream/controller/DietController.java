package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.DietRecordDTO;
import com.poundsdream.dto.SaveTemplateDTO;
import com.poundsdream.entity.Food;
import com.poundsdream.service.DietService;
import com.poundsdream.vo.DailyDietVO;
import com.poundsdream.vo.DietRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "饮食管理")
@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @Operation(summary = "添加饮食记录")
    @PostMapping
    public Result<DietRecordVO> addRecord(@Valid @RequestBody DietRecordDTO dto) {
        return Result.success(dietService.addRecord(dto));
    }

    @Operation(summary = "获取今日饮食记录")
    @GetMapping("/today")
    public Result<DailyDietVO> getTodayDiet() {
        return Result.success(dietService.getDailyDiet(LocalDate.now()));
    }

    @Operation(summary = "获取指定日期饮食记录")
    @GetMapping("/daily")
    public Result<DailyDietVO> getDailyDiet(
            @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(dietService.getDailyDiet(date));
    }

    @Operation(summary = "删除饮食记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        dietService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "搜索食物")
    @GetMapping("/food/search")
    public Result<List<Food>> searchFood(@RequestParam(value = "keyword") String keyword) {
        return Result.success(dietService.searchFood(keyword));
    }

    @Operation(summary = "添加自定义食物")
    @PostMapping("/food")
    public Result<Food> addCustomFood(@RequestBody Food food) {
        return Result.success(dietService.addCustomFood(food));
    }

    @Operation(summary = "获取食物替代建议")
    @GetMapping("/food/alternatives")
    public Result<List<Map<String, Object>>> getAlternatives(
            @RequestParam Long foodId,
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(dietService.getAlternatives(foodId, limit));
    }

    @Operation(summary = "获取AI饮食推荐")
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendations() {
        return Result.success(dietService.getRecommendations());
    }

    @Operation(summary = "保存当前餐次为模版")
    @PostMapping("/templates")
    public Result<Map<String, Object>> saveAsTemplate(@Valid @RequestBody SaveTemplateDTO dto) {
        return Result.success(dietService.saveAsTemplate(dto));
    }

    @Operation(summary = "获取饮食模版列表")
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getTemplates() {
        return Result.success(dietService.getTemplates());
    }

    @Operation(summary = "删除饮食模版")
    @DeleteMapping("/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        dietService.deleteTemplate(id);
        return Result.success();
    }

    @Operation(summary = "一键应用饮食模版")
    @PostMapping("/templates/{id}/apply")
    public Result<Void> applyTemplate(@PathVariable Long id, @RequestBody Map<String, String> body) {
        LocalDate date = body.get("date") != null ? LocalDate.parse(body.get("date")) : LocalDate.now();
        dietService.applyTemplate(id, date);
        return Result.success();
    }

    @Operation(summary = "跳过某餐")
    @PostMapping("/skip-meal")
    public Result<Void> skipMeal(@RequestParam Integer mealType,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        dietService.skipMeal(mealType, date);
        return Result.success();
    }
}
