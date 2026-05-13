package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.WaterRecordDTO;
import com.poundsdream.service.WaterService;
import com.poundsdream.vo.WaterDailyVO;
import com.poundsdream.vo.WaterDayStat;
import com.poundsdream.vo.WaterTypeStat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "饮水管理")
@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterService waterService;

    @Operation(summary = "添加饮水记录")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody WaterRecordDTO dto) {
        waterService.addRecord(dto);
        return Result.success();
    }

    @Operation(summary = "删除饮水记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        waterService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "获取今日饮水数据")
    @GetMapping("/today")
    public Result<WaterDailyVO> getToday() {
        return Result.success(waterService.getTodayData());
    }

    @Operation(summary = "更新每日饮水目标")
    @PutMapping("/goal")
    public Result<Void> updateGoal(@RequestParam Integer goal) {
        waterService.updateDailyGoal(goal);
        return Result.success();
    }

    @Operation(summary = "获取饮水方式统计")
    @GetMapping("/type-stats")
    public Result<List<WaterTypeStat>> getTypeStats(@RequestParam(defaultValue = "7") int days) {
        return Result.success(waterService.getTypeStats(days));
    }

    @Operation(summary = "获取饮水趋势数据")
    @GetMapping("/trend")
    public Result<List<WaterDayStat>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(waterService.getTrendData(days));
    }
}
