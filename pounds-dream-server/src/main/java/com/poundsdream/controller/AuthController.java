package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.dto.LoginRequest;
import com.poundsdream.dto.RegisterRequest;
import com.poundsdream.service.AuthService;
import com.poundsdream.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
}
