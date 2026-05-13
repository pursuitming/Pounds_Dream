package com.poundsdream.service;

import com.poundsdream.dto.ExerciseRecordDTO;
import com.poundsdream.entity.ExerciseType;
import com.poundsdream.vo.ExerciseRecordVO;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseService {

    ExerciseRecordVO addRecord(ExerciseRecordDTO dto);

    List<ExerciseRecordVO> getDailyRecords(LocalDate date);

    void deleteRecord(Long id);

    List<ExerciseType> getAllTypes();
}
