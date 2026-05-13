package com.poundsdream.service.impl;

import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.UserProfileDTO;
import com.poundsdream.entity.User;
import com.poundsdream.entity.WeightRecord;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.mapper.WeightRecordMapper;
import com.poundsdream.security.SecurityUtil;
import com.poundsdream.service.UserService;
import com.poundsdream.util.CalorieCalculator;
import com.poundsdream.util.DateUtil;
import com.poundsdream.vo.HealthProfileVO;
import com.poundsdream.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final WeightRecordMapper weightRecordMapper;

    @Override
    public UserVO getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserVO updateProfile(UserProfileDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        if (dto.getHeight() != null) user.setHeight(dto.getHeight());
        if (dto.getTargetWeight() != null) user.setTargetWeight(dto.getTargetWeight());
        if (dto.getTargetCalorie() != null) user.setTargetCalorie(dto.getTargetCalorie());
        if (dto.getActivityLevel() != null) user.setActivityLevel(dto.getActivityLevel());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        userMapper.updateById(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public HealthProfileVO getHealthProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取最新体重
        BigDecimal weight = user.getTargetWeight(); // 默认用目标体重
        WeightRecord latestRecord = weightRecordMapper.selectLatestByUserId(userId);
        if (latestRecord != null) {
            weight = latestRecord.getWeight();
        }

        BigDecimal height = user.getHeight();
        if (height == null) height = BigDecimal.valueOf(170);
        if (weight == null) weight = BigDecimal.valueOf(65);

        int age = DateUtil.calcAge(user.getBirthday());
        int gender = user.getGender() != null ? user.getGender() : 1;
        int activityLevel = user.getActivityLevel() != null ? user.getActivityLevel() : 2;

        // 计算 BMR
        BigDecimal bmr = CalorieCalculator.calcBMR(gender, weight, height, age);

        // 计算 TDEE
        double activityFactor = com.poundsdream.enums.ActivityLevel.fromCode(activityLevel).getFactor();
        BigDecimal tdee = CalorieCalculator.calcTDEE(bmr, activityFactor);

        // 计算目标热量 (默认每周减 0.5kg)
        BigDecimal targetCalorie = CalorieCalculator.calcTargetCalorie(tdee, 0.5);
        if (user.getTargetCalorie() != null) {
            targetCalorie = BigDecimal.valueOf(user.getTargetCalorie());
        }

        // 计算三大营养素
        BigDecimal[] macros = CalorieCalculator.calcMacros(targetCalorie);

        // 计算 BMI
        BigDecimal bmi = CalorieCalculator.calcBMI(weight, height);
        String bmiCategory = CalorieCalculator.getBMICategory(bmi);

        return HealthProfileVO.builder()
                .bmr(bmr)
                .tdee(tdee)
                .targetCalorie(targetCalorie)
                .carbGram(macros[0])
                .proteinGram(macros[1])
                .fatGram(macros[2])
                .bmi(bmi)
                .bmiCategory(bmiCategory)
                .build();
    }
}
