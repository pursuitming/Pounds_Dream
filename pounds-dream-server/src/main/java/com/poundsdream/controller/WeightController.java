package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.BodyMeasurementDTO;
import com.poundsdream.dto.WeightDTO;
import com.poundsdream.service.WeightService;
import com.poundsdream.vo.WeightRecordVO;
import com.poundsdream.vo.WeightTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "体重管理")
@RestController
@RequestMapping("/api/weight")
@RequiredArgsConstructor
public class WeightController {

    private final WeightService weightService;

    @Operation(summary = "新增/更新体重记录")
    @PostMapping
    public Result<WeightRecordVO> addOrUpdate(@Valid @RequestBody WeightDTO dto) {
        return Result.success(weightService.addOrUpdate(dto));
    }

    @Operation(summary = "获取体重趋势")
    @GetMapping("/trend")
    public Result<WeightTrendVO> getTrend(@RequestParam(value = "days") int days) {
        return Result.success(weightService.getTrend(days));
    }

    @Operation(summary = "查询体重历史")
    @GetMapping("/history")
    public Result<List<WeightRecordVO>> getHistory(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(weightService.getHistory(startDate, endDate));
    }

    @Operation(summary = "删除体重记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        weightService.delete(id);
        return Result.success();
    }

    @Operation(summary = "添加围度记录")
    @PostMapping("/measurement")
    public Result<Void> addBodyMeasurement(@RequestBody BodyMeasurementDTO dto) {
        weightService.addBodyMeasurement(dto);
        return Result.success();
    }

    @Operation(summary = "查询围度记录")
    @GetMapping("/measurements")
    public Result<List<BodyMeasurementDTO>> getBodyMeasurements(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(weightService.getBodyMeasurements(startDate, endDate));
    }
}
