package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.MenstrualCycleDTO;
import com.poundsdream.service.MenstrualCycleService;
import com.poundsdream.vo.MenstrualCycleVO;
import com.poundsdream.vo.MenstrualStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "经期管理")
@RestController
@RequestMapping("/api/menstrual")
@RequiredArgsConstructor
public class MenstrualCycleController {

    private final MenstrualCycleService menstrualCycleService;

    @Operation(summary = "添加经期记录")
    @PostMapping
    public Result<MenstrualCycleVO> add(@Valid @RequestBody MenstrualCycleDTO dto) {
        return Result.success(menstrualCycleService.addRecord(dto));
    }

    @Operation(summary = "更新经期记录")
    @PutMapping
    public Result<MenstrualCycleVO> update(@Valid @RequestBody MenstrualCycleDTO dto) {
        return Result.success(menstrualCycleService.updateRecord(dto));
    }

    @Operation(summary = "删除经期记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menstrualCycleService.deleteRecord(id);
        return Result.success();
    }

    @Operation(summary = "获取所有经期记录")
    @GetMapping
    public Result<List<MenstrualCycleVO>> getAll() {
        return Result.success(menstrualCycleService.getAllRecords());
    }

    @Operation(summary = "获取经期统计数据")
    @GetMapping("/stats")
    public Result<MenstrualStatsVO> getStats() {
        return Result.success(menstrualCycleService.getStats());
    }
}
