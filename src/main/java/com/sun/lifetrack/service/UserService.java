package com.sun.lifetrack.service;

import com.sun.lifetrack.dto.response.LoginResponse;
import com.sun.lifetrack.dto.response.UserRegisterResponse;

public interface UserService {
    /**
     * 用户注册
     * @param username 用户名
     * @param password 明文密码
     * @param email 邮箱
     * @return 注册成功的用户信息
     * @throws RuntimeException 当用户名或邮箱已存在时抛出
     */
    UserRegisterResponse register(String username, String password, String email);

    LoginResponse login(String username, String password);
}
