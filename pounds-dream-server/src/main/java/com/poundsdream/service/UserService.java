package com.poundsdream.service;

import com.poundsdream.dto.UserProfileDTO;
import com.poundsdream.vo.HealthProfileVO;
import com.poundsdream.vo.UserVO;

public interface UserService {

    UserVO getCurrentUser();

    UserVO updateProfile(UserProfileDTO dto);

    HealthProfileVO getHealthProfile();
}
