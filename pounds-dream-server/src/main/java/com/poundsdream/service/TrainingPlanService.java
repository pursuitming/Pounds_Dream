package com.poundsdream.service;

import com.poundsdream.dto.TrainingPlanDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TrainingPlanService {

    Map<String, Object> createPlan(TrainingPlanDTO dto);

    List<Map<String, Object>> getPlans();

    Map<String, Object> getPlanDetail(Long id);

    void updatePlan(Long id, TrainingPlanDTO dto);

    void deletePlan(Long id);

    Map<String, Object> getTodayPlan();
}
