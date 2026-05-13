package com.poundsdream.service;

import com.poundsdream.vo.RankVO;

public interface RankService {

    RankVO getRankInfo(Long userId);

    RankVO checkIn(Long userId);
}
