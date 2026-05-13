package com.poundsdream.service;

import com.poundsdream.dto.BodyMeasurementDTO;
import com.poundsdream.dto.WeightDTO;
import com.poundsdream.vo.WeightRecordVO;
import com.poundsdream.vo.WeightTrendVO;

import java.time.LocalDate;
import java.util.List;

public interface WeightService {

    WeightRecordVO addOrUpdate(WeightDTO dto);

    WeightTrendVO getTrend(int days);

    List<WeightRecordVO> getHistory(LocalDate startDate, LocalDate endDate);

    void delete(Long id);

    void addBodyMeasurement(BodyMeasurementDTO dto);

    List<BodyMeasurementDTO> getBodyMeasurements(LocalDate startDate, LocalDate endDate);
}
