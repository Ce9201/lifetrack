package com.sun.lifetrack.controller;

import com.sun.lifetrack.dto.request.LoginRequest;
import com.sun.lifetrack.dto.request.UserRegisterRequest;
import com.sun.lifetrack.dto.response.LoginResponse;
import com.sun.lifetrack.dto.response.UserRegisterResponse;
import com.sun.lifetrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、信息查询接口")
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "提供用户名、密码、邮箱进行注册")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        UserRegisterResponse response = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail()
        );
        return buildResponse("注册成功", response, HttpStatus.CREATED);
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT token")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求: username={}", request.getUsername());
        LoginResponse loginResponse = userService.login(request.getUsername(), request.getPassword());
        return buildResponse("登录成功", loginResponse, HttpStatus.OK);
    }

    @Operation(summary = "获取当前用户信息", description = "通过 JWT 获取已登录用户信息")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 这里可以返回一个简单的对象，也可以复用 LoginResponse，或者新建一个 UserProfileResponse
        Map<String, String> data = Map.of("username", username);
        return buildResponse("获取成功", data, HttpStatus.OK);
    }

    // 统一响应构建方法
    private ResponseEntity<Map<String, Object>> buildResponse(String message, Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", status.value());
        response.put("message", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }
}