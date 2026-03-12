package com.sun.lifetrack.service.impl;

import com.sun.lifetrack.dto.response.LoginResponse;
import com.sun.lifetrack.dto.response.UserRegisterResponse;
import com.sun.lifetrack.entity.User;
import com.sun.lifetrack.exception.BusinessException;
import com.sun.lifetrack.exception.ErrorCode;
import com.sun.lifetrack.repository.UserRepository;
import com.sun.lifetrack.service.UserService;
import com.sun.lifetrack.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    //邮箱密码格式正则表达式
    //john.doe@my-site.co.uk
    //fgbg1dvfb55f%%
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );//本地部分：必须由字母数字列出的特殊字符组成，长度至少为1。二级域名：由@开头，必须由字母数字.-组成，长度至少为1。顶级域名：由点开头，紧跟 2~6 个大小写字母
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d).+$"
    );//存在至少一个字母,存在至少一个数字
    private final JwtUtil jwtUtil;



    //---------- 注册register方法 ----------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse register(String username, String password, String email) {
        log.info("开始用户注册流程 - 用户名: {}", username);

        //1.清洗所有输入（统一去除首尾空格）
        String cleanUsername = StringUtils.trimWhitespace(username);
        String cleanPassword = StringUtils.trimWhitespace(password);
        String cleanEmail = StringUtils.trimWhitespace(email);

        //2.按字段分组校验（每个字段的完整校验集中在一起）
        validateUsername(cleanUsername);
        validatePassword(cleanPassword);
        validateEmail(cleanEmail);

        //3.构建用户实体（邮箱已处理为统一小写）
        User user = new User();
        user.setUsername(cleanUsername);
        String encryptedPassword = passwordEncoder.encode(cleanPassword);
        user.setPasswordHash(encryptedPassword);
        user.setEmail(cleanEmail.toLowerCase());

        //4.持久化
        User savedUser = userRepository.save(user);
        log.info("用户注册成功 - ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        return UserRegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .build();
    }

    @Override
    public LoginResponse login(String username, String password){
        // 1. 清洗输入
        String cleanUsername = StringUtils.trimWhitespace(username);
        String cleanPassword = StringUtils.trimWhitespace(password);

        if (!StringUtils.hasText(cleanUsername) || !StringUtils.hasText(cleanPassword)) {
            throw new BusinessException(ErrorCode.USERNAME_PASSWORD_EMPTY);
        }

        //2. 查找用户
        User user = userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERNAME_NOT_FOUND));

        //3. 验证密码
        if (!passwordEncoder.matches(cleanPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT);
        }

        // 4. 生成 JWT
        String token = jwtUtil.generateToken(user.getUsername());
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build();
    }



    //---------- 校验方法 ----------
    //用户名校验
    private void validateUsername(String username) {
        //非空校验
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EMPTY);
        }
        //长度校验（可根据需求调整）
        if (username.length() < 2 || username.length() > 20) {
            throw new BusinessException(ErrorCode.USERNAME_INVALID_LENGTH);
        }
        //唯一性校验
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            log.warn("用户名已存在: {}", username);
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }
    //密码校验
    private void validatePassword(String password) {
        //非空
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.PASSWORD_EMPTY);
        }
        //长度 >=6&&<=20（增加最大长度限制，防止超长密码导致DOS）
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }
        if (password.length() > 20) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_LONG);
        }
        //格式强度校验（至少包含字母和数字）
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
        }
    }
    //邮箱校验
    private void validateEmail(String email) {
        //非空
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.EMAIL_EMPTY);
        }
        //格式校验
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID_FORMAT);
        }
        //唯一性校验
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            log.warn("邮箱已存在: {}", email);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

}
