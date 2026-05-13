package com.poundsdream.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalorieCalculator {

    /**
     * 计算 BMR (基础代谢率) - Mifflin-St Jeor 公式
     * 男: BMR = 10 * 体重(kg) + 6.25 * 身高(cm) - 5 * 年龄 + 5
     * 女: BMR = 10 * 体重(kg) + 6.25 * 身高(cm) - 5 * 年龄 - 161
     */
    public static BigDecimal calcBMR(int gender, BigDecimal weight, BigDecimal height, int age) {
        double bmr = 10 * weight.doubleValue() + 6.25 * height.doubleValue() - 5 * age;
        if (gender == 1) {
            bmr += 5;
        } else {
            bmr -= 161;
        }
        return BigDecimal.valueOf(bmr).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 计算 TDEE (每日能量总消耗)
     * TDEE = BMR * PAL 系数
     */
    public static BigDecimal calcTDEE(BigDecimal bmr, double activityFactor) {
        return bmr.multiply(BigDecimal.valueOf(activityFactor)).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 根据减重目标计算每日推荐摄入热量
     * 1kg 脂肪 ≈ 7700kcal
     * 每周减 0.5kg = 每天缺口 550kcal
     */
    public static BigDecimal calcTargetCalorie(BigDecimal tdee, double weeklyWeightLossKg) {
        double dailyDeficit = weeklyWeightLossKg * 7700 / 7;
        double target = tdee.doubleValue() - dailyDeficit;
        // 最低不低于 1200kcal
        if (target < 1200) {
            target = 1200;
        }
        return BigDecimal.valueOf(target).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 计算三大营养素分配 (4:3:3 比例)
     * 碳水 4kcal/g, 蛋白质 4kcal/g, 脂肪 9kcal/g
     */
    public static BigDecimal[] calcMacros(BigDecimal targetCalorie) {
        double cal = targetCalorie.doubleValue();
        double carbGram = cal * 0.4 / 4;   // 碳水 40%
        double proteinGram = cal * 0.3 / 4; // 蛋白质 30%
        double fatGram = cal * 0.3 / 9;     // 脂肪 30%
        return new BigDecimal[]{
                BigDecimal.valueOf(carbGram).setScale(0, RoundingMode.HALF_UP),
                BigDecimal.valueOf(proteinGram).setScale(0, RoundingMode.HALF_UP),
                BigDecimal.valueOf(fatGram).setScale(0, RoundingMode.HALF_UP)
        };
    }

    /**
     * 计算运动消耗热量
     * 消耗热量(kcal) = MET值 * 体重(kg) * 运动时长(小时)
     */
    public static int calcExerciseCalorie(double metValue, double weightKg, int durationMinutes) {
        return (int) Math.round(metValue * weightKg * (durationMinutes / 60.0));
    }

    /**
     * 计算食物热量
     * 摄入热量 = (每100g热量 / 100) * 食用克数
     */
    public static int calcFoodCalorie(int caloriePer100g, double amountGram) {
        return (int) Math.round(caloriePer100g * amountGram / 100.0);
    }

    /**
     * 计算 BMI
     * BMI = 体重(kg) / (身高(m))^2
     */
    public static BigDecimal calcBMI(BigDecimal weight, BigDecimal heightCm) {
        double heightM = heightCm.doubleValue() / 100.0;
        double bmi = weight.doubleValue() / (heightM * heightM);
        return BigDecimal.valueOf(bmi).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 获取 BMI 分类
     */
    public static String getBMICategory(BigDecimal bmi) {
        double value = bmi.doubleValue();
        if (value < 18.5) return "偏瘦";
        if (value < 24) return "正常";
        if (value < 28) return "偏胖";
        return "肥胖";
    }

    /**
     * 计算体脂率 - Deurenberg 公式
     * 体脂率 = 1.2 × BMI + 0.23 × 年龄 - 10.8 × 性别（男=1，女=0）- 5.4
     *
     * @param bmi BMI值
     * @param age 年龄
     * @param gender 性别（1=男，0=女）
     * @return 体脂率（百分比）
     */
    public static BigDecimal calcBodyFat(BigDecimal bmi, int age, int gender) {
        double bodyFat = 1.2 * bmi.doubleValue() + 0.23 * age - 10.8 * gender - 5.4;
        // 限制在合理范围内（男性 3-25%，女性 10-35%）
        if (gender == 1) {
            bodyFat = Math.max(3, Math.min(25, bodyFat));
        } else {
            bodyFat = Math.max(10, Math.min(35, bodyFat));
        }
        return BigDecimal.valueOf(bodyFat).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 根据生日计算年龄
     *
     * @param birthday 生日
     * @return 年龄
     */
    public static int calcAge(java.time.LocalDate birthday) {
        if (birthday == null) return 25; // 默认年龄
        java.time.LocalDate today = java.time.LocalDate.now();
        int age = today.getYear() - birthday.getYear();
        if (today.getDayOfYear() < birthday.getDayOfYear()) {
            age--;
        }
        return Math.max(1, age);
    }
}
