package com.poundsdream.service;

import com.poundsdream.dto.WaterRecordDTO;
import com.poundsdream.vo.WaterDailyVO;
import com.poundsdream.vo.WaterDayStat;
import com.poundsdream.vo.WaterTypeStat;

import java.util.List;

public interface WaterService {

    void addRecord(WaterRecordDTO dto);

    void deleteRecord(Long id);

    WaterDailyVO getTodayData();

    void updateDailyGoal(Integer goal);

    List<WaterTypeStat> getTypeStats(int days);

    List<WaterDayStat> getTrendData(int days);
}
