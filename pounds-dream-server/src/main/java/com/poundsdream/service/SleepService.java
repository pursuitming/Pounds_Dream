package com.poundsdream.service;

import com.poundsdream.dto.SleepRecordDTO;
import com.poundsdream.vo.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface SleepService {

    void addRecord(SleepRecordDTO dto);

    void deleteRecord(Long id);

    SleepDailyVO getTodayData();

    void updateDailyGoal(Integer goal);

    List<SleepTrendVO> getTrend(int days);

    SleepRegularityVO getRegularity();

    SleepRecommendationVO getRecommendation();

    String getAiAdvice();

    void streamAiAdvice(HttpServletResponse response);
}
