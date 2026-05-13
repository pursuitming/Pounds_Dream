package com.poundsdream.service;

import com.poundsdream.dto.DietRecordDTO;
import com.poundsdream.dto.SaveTemplateDTO;
import com.poundsdream.entity.Food;
import com.poundsdream.vo.DailyDietVO;
import com.poundsdream.vo.DietRecordVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DietService {

    DietRecordVO addRecord(DietRecordDTO dto);

    DailyDietVO getDailyDiet(LocalDate date);

    void deleteRecord(Long id);

    List<Food> searchFood(String keyword);

    Food addCustomFood(Food food);

    List<Map<String, Object>> getAlternatives(Long foodId, int limit);

    List<Map<String, Object>> getRecommendations();

    Map<String, Object> saveAsTemplate(SaveTemplateDTO dto);

    List<Map<String, Object>> getTemplates();

    void deleteTemplate(Long id);

    void applyTemplate(Long templateId, LocalDate date);

    void skipMeal(Integer mealType, LocalDate date);
}
