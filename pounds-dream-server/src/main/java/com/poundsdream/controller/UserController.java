package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.UserProfileDTO;
import com.poundsdream.service.UserService;
import com.poundsdream.vo.HealthProfileVO;
import com.poundsdream.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        return Result.success(userService.getCurrentUser());
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody UserProfileDTO dto) {
        return Result.success(userService.updateProfile(dto));
    }

    @Operation(summary = "获取健康画像")
    @GetMapping("/health-profile")
    public Result<HealthProfileVO> getHealthProfile() {
        return Result.success(userService.getHealthProfile());
    }
}
