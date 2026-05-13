package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.BodyMeasurementDTO;
import com.poundsdream.dto.WeightDTO;
import com.poundsdream.entity.BodyMeasurement;
import com.poundsdream.entity.User;
import com.poundsdream.entity.WeightRecord;
import com.poundsdream.mapper.BodyMeasurementMapper;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.mapper.WeightRecordMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.RankService;
import com.poundsdream.service.UserService;
import com.poundsdream.service.WeightService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.vo.WeightRecordVO;
import com.poundsdream.vo.WeightTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeightServiceImpl implements WeightService {

    private final WeightRecordMapper weightRecordMapper;
    private final BodyMeasurementMapper bodyMeasurementMapper;
    private final UserMapper userMapper;
    private final RankService rankService;

    @Override
    public WeightRecordVO addOrUpdate(WeightDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate recordDate = dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now();

        // 检查是否已有当日记录
        WeightRecord existing = weightRecordMapper.selectOne(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .eq(WeightRecord::getRecordDate, recordDate));

        // 获取用户信息计算 BMI 和体脂率
        User user = userMapper.selectById(userId);
        BigDecimal bmi = null;
        BigDecimal bodyFat = dto.getBodyFat();

        if (user != null && user.getHeight() != null) {
            bmi = CalorieCalculator.calcBMI(dto.getWeight(), user.getHeight());

            // 如果用户没有输入体脂率，自动计算
            if (bodyFat == null && bmi != null) {
                // 转换性别格式：数据库 0=未知,1=男,2=女 -> 公式 1=男,0=女
                int genderForCalc = (user.getGender() != null && user.getGender() == 1) ? 1 : 0;
                int age = CalorieCalculator.calcAge(user.getBirthday());
                bodyFat = CalorieCalculator.calcBodyFat(bmi, age, genderForCalc);
            }
        }

        WeightRecord result;
        if (existing != null) {
            existing.setWeight(dto.getWeight());
            existing.setBodyFat(bodyFat);
            existing.setBmi(bmi);
            existing.setNote(dto.getNote());
            weightRecordMapper.updateById(existing);
            result = existing;
        } else {
            WeightRecord record = new WeightRecord();
            record.setUserId(userId);
            record.setWeight(dto.getWeight());
            record.setBodyFat(bodyFat);
            record.setBmi(bmi);
            record.setRecordDate(recordDate);
            record.setNote(dto.getNote());
            weightRecordMapper.insert(record);
            result = record;
        }

        // 触发段位打卡检查
        try { rankService.checkIn(userId); } catch (Exception e) { log.warn("段位打卡检查失败", e); }

        return convertToVO(result);
    }

    @Override
    public WeightTrendVO getTrend(int days) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate startDate = LocalDate.now().minusDays(days);

        List<WeightRecord> records = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .ge(WeightRecord::getRecordDate, startDate)
                        .orderByAsc(WeightRecord::getRecordDate));

        List<WeightRecordVO> recordVOs = records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        BigDecimal startWeight = records.isEmpty() ? null : records.get(0).getWeight();
        BigDecimal currentWeight = records.isEmpty() ? null : records.get(records.size() - 1).getWeight();
        BigDecimal totalChange = null;
        BigDecimal averageChange = null;

        if (startWeight != null && currentWeight != null) {
            totalChange = currentWeight.subtract(startWeight);
            if (records.size() > 1) {
                averageChange = totalChange.divide(BigDecimal.valueOf(records.size() - 1), 2, RoundingMode.HALF_UP);
            }
        }

        return WeightTrendVO.builder()
                .records(recordVOs)
                .startWeight(startWeight)
                .currentWeight(currentWeight)
                .totalChange(totalChange)
                .averageChange(averageChange)
                .build();
    }

    @Override
    public List<WeightRecordVO> getHistory(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<WeightRecord> records = weightRecordMapper.selectList(
                new LambdaQueryWrapper<WeightRecord>()
                        .eq(WeightRecord::getUserId, userId)
                        .ge(startDate != null, WeightRecord::getRecordDate, startDate)
                        .le(endDate != null, WeightRecord::getRecordDate, endDate)
                        .orderByDesc(WeightRecord::getRecordDate));

        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        WeightRecord record = weightRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        weightRecordMapper.deleteById(id);
    }

    @Override
    public void addBodyMeasurement(BodyMeasurementDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate recordDate = dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now();

        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setUserId(userId);
        measurement.setWaist(dto.getWaist());
        measurement.setHip(dto.getHip());
        measurement.setChest(dto.getChest());
        measurement.setUpperArm(dto.getUpperArm());
        measurement.setThigh(dto.getThigh());
        measurement.setRecordDate(recordDate);
        bodyMeasurementMapper.insert(measurement);
    }

    @Override
    public List<BodyMeasurementDTO> getBodyMeasurements(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<BodyMeasurement> measurements = bodyMeasurementMapper.selectList(
                new LambdaQueryWrapper<BodyMeasurement>()
                        .eq(BodyMeasurement::getUserId, userId)
                        .ge(startDate != null, BodyMeasurement::getRecordDate, startDate)
                        .le(endDate != null, BodyMeasurement::getRecordDate, endDate)
                        .orderByDesc(BodyMeasurement::getRecordDate));

        return measurements.stream().map(m -> {
            BodyMeasurementDTO dto = new BodyMeasurementDTO();
            BeanUtils.copyProperties(m, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    private WeightRecordVO convertToVO(WeightRecord record) {
        WeightRecordVO vo = new WeightRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
