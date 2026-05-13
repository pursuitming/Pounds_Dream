package com.poundsdream.service;

import com.poundsdream.vo.RecommendedTrainingVO;

public interface TrainingRecommendService {

    /**
     * 获取推荐训练计划
     * @param intensity 强度：LOW, MEDIUM, HIGH
     * @return 推荐训练
     */
    RecommendedTrainingVO getRecommendedTraining(String intensity);
}
