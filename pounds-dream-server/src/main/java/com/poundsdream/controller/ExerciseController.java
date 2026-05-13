package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.ExerciseRecordDTO;
import com.poundsdream.entity.ExerciseType;
import com.poundsdream.service.ExerciseService;
import com.poundsdream.vo.ExerciseRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "运动管理")
@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(summary = "添加运动记录")
    @PostMapping
    public Result<ExerciseRecordVO> addRecord(@Valid @RequestBody ExerciseRecordDTO dto) {
        return Result.success(exerciseService.addRecord(dto));
    }

    @Operation(summary = "获取今日运动记录")
    @GetMapping("/today")
    public Result<List<ExerciseRecordVO>> getTodayRecords() {
        return Result.success(exerciseService.getDailyRecords(LocalDate.now()));
    }

    @Operation(summary = "获取指定日期运动记录")
    @GetMapping("/daily")
    public Result<List<ExerciseRecordVO>> getDailyRecords(
            @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(exerciseService.getDailyRecords(date));
    }

    @Operation(summary = "删除运动记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        exerciseService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "获取所有运动类型")
    @GetMapping("/types")
    public Result<List<ExerciseType>> getAllTypes() {
        return Result.success(exerciseService.getAllTypes());
    }
}
