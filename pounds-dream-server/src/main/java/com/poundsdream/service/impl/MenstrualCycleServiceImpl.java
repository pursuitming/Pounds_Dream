package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.dto.MenstrualCycleDTO;
import com.poundsdream.entity.MenstrualCycle;
import com.poundsdream.mapper.MenstrualCycleMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.MenstrualCycleService;
import com.poundsdream.vo.MenstrualCycleVO;
import com.poundsdream.vo.MenstrualStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenstrualCycleServiceImpl implements MenstrualCycleService {

    private final MenstrualCycleMapper menstrualCycleMapper;

    @Override
    public MenstrualCycleVO addRecord(MenstrualCycleDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        MenstrualCycle entity = new MenstrualCycle();
        entity.setUserId(userId);
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSymptoms(dto.getSymptoms());
        entity.setMood(dto.getMood());
        entity.setNote(dto.getNote());

        // 计算经期天数
        if (dto.getEndDate() != null) {
            int periodDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
            entity.setPeriodLength(periodDays);
        }

        // 计算周期长度（与上一次记录的间隔）
        MenstrualCycle latest = menstrualCycleMapper.selectLatestByUserId(userId);
        if (latest != null && latest.getStartDate() != null) {
            int cycleLen = (int) ChronoUnit.DAYS.between(latest.getStartDate(), dto.getStartDate());
            if (cycleLen > 0 && cycleLen < 60) {
                entity.setCycleLength(cycleLen);
            }
        }

        menstrualCycleMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public MenstrualCycleVO updateRecord(MenstrualCycleDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        MenstrualCycle entity = menstrualCycleMapper.selectById(dto.getId());
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在");
        }

        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSymptoms(dto.getSymptoms());
        entity.setMood(dto.getMood());
        entity.setNote(dto.getNote());

        if (dto.getEndDate() != null) {
            int periodDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
            entity.setPeriodLength(periodDays);
        }

        menstrualCycleMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        MenstrualCycle entity = menstrualCycleMapper.selectById(id);
        if (entity != null && entity.getUserId().equals(userId)) {
            menstrualCycleMapper.deleteById(id);
        }
    }

    @Override
    public List<MenstrualCycleVO> getAllRecords() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<MenstrualCycle> records = menstrualCycleMapper.selectAllByUserId(userId);
        return records.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public MenstrualStatsVO getStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<MenstrualCycle> records = menstrualCycleMapper.selectAllByUserId(userId);

        if (records.isEmpty()) {
            return MenstrualStatsVO.builder()
                    .recentCycles(Collections.emptyList())
                    .build();
        }

        // 计算统计数据
        List<Integer> cycleLengths = records.stream()
                .map(MenstrualCycle::getCycleLength)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Integer> periodLengths = records.stream()
                .map(MenstrualCycle::getPeriodLength)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Integer avgCycle = cycleLengths.isEmpty() ? 28 :
                (int) cycleLengths.stream().mapToInt(Integer::intValue).average().orElse(28);
        Integer avgPeriod = periodLengths.isEmpty() ? 5 :
                (int) periodLengths.stream().mapToInt(Integer::intValue).average().orElse(5);
        Integer minCycle = cycleLengths.isEmpty() ? 28 : Collections.min(cycleLengths);
        Integer maxCycle = cycleLengths.isEmpty() ? 28 : Collections.max(cycleLengths);

        // 构建最近6条记录的VO（含预测信息）
        List<MenstrualCycleVO> recentCycles = new ArrayList<>();
        for (int i = 0; i < Math.min(6, records.size()); i++) {
            MenstrualCycle record = records.get(i);
            MenstrualCycleVO vo = toVO(record);

            // 预测下次经期
            if (record.getCycleLength() != null) {
                vo.setNextPredictedDate(record.getStartDate().plusDays(record.getCycleLength()));
            } else {
                vo.setNextPredictedDate(record.getStartDate().plusDays(avgCycle));
            }

            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), vo.getNextPredictedDate());
            vo.setDaysUntilNext((int) daysUntil);

            // 当前阶段
            vo.setCurrentPhase(calcPhase(record, avgCycle));

            recentCycles.add(vo);
        }

        // 当前周期信息
        MenstrualCycle latest = records.get(0);
        MenstrualCycleVO currentCycle = recentCycles.isEmpty() ? null : recentCycles.get(0);

        return MenstrualStatsVO.builder()
                .avgCycleLength(avgCycle)
                .avgPeriodLength(avgPeriod)
                .minCycleLength(minCycle)
                .maxCycleLength(maxCycle)
                .recentCycles(recentCycles)
                .currentCycle(currentCycle)
                .build();
    }

    private String calcPhase(MenstrualCycle record, int avgCycle) {
        LocalDate today = LocalDate.now();
        LocalDate start = record.getStartDate();
        int daysSinceStart = (int) ChronoUnit.DAYS.between(start, today);

        if (daysSinceStart < 0) return "未知";
        if (record.getEndDate() == null || today.isBefore(record.getEndDate()) || today.equals(record.getEndDate())) {
            return "月经期";
        }

        int periodDays = record.getPeriodLength() != null ? record.getPeriodLength() : 5;
        int ovulationDay = avgCycle - 14;

        if (daysSinceStart <= periodDays + 7) return "卵泡期";
        if (daysSinceStart <= ovulationDay + 2) return "排卵期";
        if (daysSinceStart <= avgCycle) return "黄体期";
        return "月经期";
    }

    private MenstrualCycleVO toVO(MenstrualCycle entity) {
        return MenstrualCycleVO.builder()
                .id(entity.getId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .cycleLength(entity.getCycleLength())
                .periodLength(entity.getPeriodLength())
                .symptoms(entity.getSymptoms())
                .mood(entity.getMood())
                .note(entity.getNote())
                .build();
    }
}
