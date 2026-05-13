package com.poundsdream.controller;

import com.poundsdream.common.Result;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.PetService;
import com.poundsdream.vo.PetLogVO;
import com.poundsdream.vo.PetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数字孪生宠物")
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(summary = "获取宠物信息")
    @GetMapping
    public Result<PetVO> getPetInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(petService.getPetInfo(userId));
    }

    @Operation(summary = "修改宠物昵称")
    @PutMapping("/name")
    public Result<Void> renamePet(@RequestParam String name) {
        Long userId = SecurityUtil.getCurrentUserId();
        petService.renamePet(userId, name);
        return Result.success();
    }

    @Operation(summary = "与宠物互动")
    @PostMapping("/interact")
    public Result<PetVO> interact(@RequestParam String action) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(petService.interact(userId, action));
    }

    @Operation(summary = "修改宠物形象")
    @PutMapping("/type")
    public Result<Void> changePetType(@RequestParam String petType) {
        Long userId = SecurityUtil.getCurrentUserId();
        petService.changePetType(userId, petType);
        return Result.success();
    }

    @Operation(summary = "获取成长日志")
    @GetMapping("/logs")
    public Result<List<PetLogVO>> getPetLogs() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(petService.getPetLogs(userId));
    }

    @Operation(summary = "饮水达标加经验")
    @PostMapping("/water-exp")
    public Result<PetVO> waterExp() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(petService.addWaterExp(userId));
    }

    @Operation(summary = "睡眠达标加经验")
    @PostMapping("/sleep-exp")
    public Result<PetVO> sleepExp() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(petService.addSleepExp(userId));
    }
}
