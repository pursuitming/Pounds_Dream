package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.ExerciseRecordDTO;
import com.poundsdream.entity.ExerciseRecord;
import com.poundsdream.entity.ExerciseType;
import com.poundsdream.entity.User;
import com.poundsdream.entity.WeightRecord;
import com.poundsdream.mapper.ExerciseRecordMapper;
import com.poundsdream.mapper.ExerciseTypeMapper;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.mapper.WeightRecordMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.ExerciseService;
import com.poundsdream.service.RankService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.ExerciseRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRecordMapper exerciseRecordMapper;
    private final ExerciseTypeMapper exerciseTypeMapper;
    private final UserMapper userMapper;
    private final WeightRecordMapper weightRecordMapper;
    private final RankService rankService;

    @Override
    public ExerciseRecordVO addRecord(ExerciseRecordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate recordDate = dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now();

        ExerciseType exerciseType = exerciseTypeMapper.selectById(dto.getExerciseTypeId());
        if (exerciseType == null) {
            throw new BusinessException(ErrorCode.EXERCISE_TYPE_NOT_FOUND);
        }

        // 获取用户体重用于计算消耗
        double weightKg = 65.0; // 默认体重
        WeightRecord latestWeight = weightRecordMapper.selectLatestByUserId(userId);
        if (latestWeight != null) {
            weightKg = latestWeight.getWeight().doubleValue();
        }

        int calorieBurned = CalorieCalculator.calcExerciseCalorie(
                exerciseType.getMetValue().doubleValue(), weightKg, dto.getDuration());

        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setExerciseTypeId(dto.getExerciseTypeId());
        record.setDuration(dto.getDuration());
        record.setCalorieBurned(calorieBurned);
        record.setHeartRate(dto.getHeartRate());
        record.setRecordDate(recordDate);
        record.setNote(dto.getNote());
        exerciseRecordMapper.insert(record);

        // 触发段位打卡检查
        try { rankService.checkIn(userId); } catch (Exception e) { log.warn("段位打卡检查失败", e); }

        return convertToVO(record, exerciseType);
    }

    @Override
    public List<ExerciseRecordVO> getDailyRecords(LocalDate date) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (date == null) date = LocalDate.now();

        List<ExerciseRecord> records = exerciseRecordMapper.selectList(
                new LambdaQueryWrapper<ExerciseRecord>()
                        .eq(ExerciseRecord::getUserId, userId)
                        .eq(ExerciseRecord::getRecordDate, date)
                        .orderByDesc(ExerciseRecord::getCreatedAt));

        return records.stream().map(r -> {
            ExerciseType type = exerciseTypeMapper.selectById(r.getExerciseTypeId());
            return convertToVO(r, type);
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        ExerciseRecord record = exerciseRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        exerciseRecordMapper.deleteById(id);
    }

    @Override
    public List<ExerciseType> getAllTypes() {
        return exerciseTypeMapper.selectList(null);
    }

    private ExerciseRecordVO convertToVO(ExerciseRecord record, ExerciseType type) {
        ExerciseRecordVO vo = new ExerciseRecordVO();
        vo.setId(record.getId());
        vo.setExerciseTypeId(record.getExerciseTypeId());
        vo.setDuration(record.getDuration());
        vo.setCalorieBurned(record.getCalorieBurned());
        vo.setHeartRate(record.getHeartRate());
        vo.setRecordDate(record.getRecordDate());
        vo.setNote(record.getNote());
        if (type != null) {
            vo.setExerciseName(type.getName());
            vo.setExerciseCategory(type.getCategory());
        }
        return vo;
    }
}
