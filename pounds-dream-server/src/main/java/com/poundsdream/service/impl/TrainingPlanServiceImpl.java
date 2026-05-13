package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.TrainingPlanDTO;
import com.poundsdream.entity.ExerciseType;
import com.poundsdream.entity.TrainingPlan;
import com.poundsdream.entity.TrainingPlanItem;
import com.poundsdream.mapper.ExerciseTypeMapper;
import com.poundsdream.mapper.TrainingPlanItemMapper;
import com.poundsdream.mapper.TrainingPlanMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanMapper trainingPlanMapper;
    private final TrainingPlanItemMapper trainingPlanItemMapper;
    private final ExerciseTypeMapper exerciseTypeMapper;

    @Override
    @Transactional
    public Map<String, Object> createPlan(TrainingPlanDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        TrainingPlan plan = new TrainingPlan();
        plan.setUserId(userId);
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        plan.setStatus(1);
        trainingPlanMapper.insert(plan);

        if (dto.getItems() != null) {
            for (TrainingPlanDTO.TrainingPlanItemDTO itemDto : dto.getItems()) {
                TrainingPlanItem item = new TrainingPlanItem();
                item.setPlanId(plan.getId());
                item.setDayOfWeek(itemDto.getDayOfWeek());
                item.setExerciseTypeId(itemDto.getExerciseTypeId());
                item.setSets(itemDto.getSets());
                item.setReps(itemDto.getReps());
                item.setDuration(itemDto.getDuration());
                item.setNote(itemDto.getNote() != null ? itemDto.getNote() : "");
                trainingPlanItemMapper.insert(item);
            }
        }

        return buildPlanVO(plan, dto.getItems() != null ? loadItems(plan.getId()) : new ArrayList<>());
    }

    @Override
    public List<Map<String, Object>> getPlans() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<TrainingPlan> plans = trainingPlanMapper.selectList(
                new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getUserId, userId)
                        .orderByDesc(TrainingPlan::getUpdatedAt));

        return plans.stream().map(p -> {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", p.getId());
            vo.put("name", p.getName());
            vo.put("description", p.getDescription());
            vo.put("status", p.getStatus());
            vo.put("createdAt", p.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPlanDetail(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        TrainingPlan plan = trainingPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        List<TrainingPlanItem> items = loadItems(id);
        return buildPlanVO(plan, items);
    }

    @Override
    @Transactional
    public void updatePlan(Long id, TrainingPlanDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        TrainingPlan plan = trainingPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        trainingPlanMapper.updateById(plan);

        // 删除旧明细，插入新明细
        trainingPlanItemMapper.delete(
                new LambdaQueryWrapper<TrainingPlanItem>()
                        .eq(TrainingPlanItem::getPlanId, id));

        if (dto.getItems() != null) {
            for (TrainingPlanDTO.TrainingPlanItemDTO itemDto : dto.getItems()) {
                TrainingPlanItem item = new TrainingPlanItem();
                item.setPlanId(id);
                item.setDayOfWeek(itemDto.getDayOfWeek());
                item.setExerciseTypeId(itemDto.getExerciseTypeId());
                item.setSets(itemDto.getSets());
                item.setReps(itemDto.getReps());
                item.setDuration(itemDto.getDuration());
                item.setNote(itemDto.getNote() != null ? itemDto.getNote() : "");
                trainingPlanItemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        TrainingPlan plan = trainingPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        trainingPlanMapper.deleteById(id);
        trainingPlanItemMapper.delete(
                new LambdaQueryWrapper<TrainingPlanItem>()
                        .eq(TrainingPlanItem::getPlanId, id));
    }

    @Override
    public Map<String, Object> getTodayPlan() {
        Long userId = SecurityUtil.getCurrentUserId();
        int todayDow = LocalDate.now().getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

        // 找到用户进行中的计划
        TrainingPlan plan = trainingPlanMapper.selectOne(
                new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getUserId, userId)
                        .eq(TrainingPlan::getStatus, 1)
                        .last("LIMIT 1"));

        if (plan == null) {
            return null;
        }

        List<TrainingPlanItem> allItems = loadItems(plan.getId());
        List<TrainingPlanItem> todayItems = allItems.stream()
                .filter(i -> i.getDayOfWeek() != null && i.getDayOfWeek() == todayDow)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("planId", plan.getId());
        result.put("planName", plan.getName());
        result.put("dayOfWeek", todayDow);
        result.put("dayName", getDayName(todayDow));
        result.put("items", todayItems.stream().map(this::buildItemVO).collect(Collectors.toList()));
        return result;
    }

    private List<TrainingPlanItem> loadItems(Long planId) {
        return trainingPlanItemMapper.selectList(
                new LambdaQueryWrapper<TrainingPlanItem>()
                        .eq(TrainingPlanItem::getPlanId, planId)
                        .orderByAsc(TrainingPlanItem::getDayOfWeek));
    }

    private Map<String, Object> buildPlanVO(TrainingPlan plan, List<TrainingPlanItem> items) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", plan.getId());
        vo.put("name", plan.getName());
        vo.put("description", plan.getDescription());
        vo.put("status", plan.getStatus());
        vo.put("createdAt", plan.getCreatedAt());
        vo.put("items", items.stream().map(this::buildItemVO).collect(Collectors.toList()));
        return vo;
    }

    private Map<String, Object> buildItemVO(TrainingPlanItem item) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", item.getId());
        vo.put("dayOfWeek", item.getDayOfWeek());
        vo.put("dayName", getDayName(item.getDayOfWeek()));
        vo.put("exerciseTypeId", item.getExerciseTypeId());
        vo.put("sets", item.getSets());
        vo.put("reps", item.getReps());
        vo.put("duration", item.getDuration());
        vo.put("note", item.getNote());

        // 查询运动类型名称
        ExerciseType et = exerciseTypeMapper.selectById(item.getExerciseTypeId());
        if (et != null) {
            vo.put("exerciseTypeName", et.getName());
            vo.put("exerciseCategory", et.getCategory());
        }
        return vo;
    }

    private String getDayName(int dow) {
        return switch (dow) {
            case 1 -> "周一";
            case 2 -> "周二";
            case 3 -> "周三";
            case 4 -> "周四";
            case 5 -> "周五";
            case 6 -> "周六";
            case 7 -> "周日";
            default -> "";
        };
    }
}
