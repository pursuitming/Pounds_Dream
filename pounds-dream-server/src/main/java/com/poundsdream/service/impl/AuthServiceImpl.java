package com.poundsdream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poundsdream.common.BusinessException;
import com.poundsdream.common.ErrorCode;
import com.poundsdream.dto.LoginRequest;
import com.poundsdream.dto.RegisterRequest;
import com.poundsdream.entity.User;
import com.poundsdream.mapper.UserMapper;
import com.poundsdream.security.JwtTokenProvider;
import com.poundsdream.service.AuthService;
import com.poundsdream.vo.LoginVO;
import com.poundsdream.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginVO register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查手机号是否已注册
        if (request.getPhone() != null) {
            count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
            if (count > 0) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        userMapper.insert(user);

        // 生成 Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        UserVO userVO = convertToUserVO(user);
        return LoginVO.builder().token(token).refreshToken(refreshToken).user(userVO).build();
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        UserVO userVO = convertToUserVO(user);
        return LoginVO.builder().token(token).refreshToken(refreshToken).user(userVO).build();
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String newToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        UserVO userVO = convertToUserVO(user);
        return LoginVO.builder().token(newToken).refreshToken(newRefreshToken).user(userVO).build();
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
