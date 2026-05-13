package com.poundsdream.service;

import com.poundsdream.dto.LoginRequest;
import com.poundsdream.dto.RegisterRequest;
import com.poundsdream.vo.LoginVO;

public interface AuthService {

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    LoginVO refreshToken(String refreshToken);
}
