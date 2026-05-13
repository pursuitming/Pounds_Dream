package com.poundsdream.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private String token;
    private String refreshToken;
    private UserVO user;
}
