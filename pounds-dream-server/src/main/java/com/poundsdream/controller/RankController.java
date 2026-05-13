package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.RankService;
import com.poundsdream.vo.RankVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "段位系统")
@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @Operation(summary = "获取段位信息")
    @GetMapping
    public Result<RankVO> getRankInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(rankService.getRankInfo(userId));
    }

    @Operation(summary = "打卡（检查连续天数并更新段位）")
    @PostMapping("/check-in")
    public Result<RankVO> checkIn() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(rankService.checkIn(userId));
    }
}
