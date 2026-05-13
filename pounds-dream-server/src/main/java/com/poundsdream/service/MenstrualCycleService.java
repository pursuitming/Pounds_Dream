package com.poundsdream.service;

import com.poundsdream.dto.MenstrualCycleDTO;
import com.poundsdream.vo.MenstrualCycleVO;
import com.poundsdream.vo.MenstrualStatsVO;

import java.util.List;

public interface MenstrualCycleService {

    MenstrualCycleVO addRecord(MenstrualCycleDTO dto);

    MenstrualCycleVO updateRecord(MenstrualCycleDTO dto);

    void deleteRecord(Long id);

    List<MenstrualCycleVO> getAllRecords();

    MenstrualStatsVO getStats();
}
