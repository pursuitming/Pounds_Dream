package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.service.DashboardService;
import com.poundsdream.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取首页仪表盘数据")
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
