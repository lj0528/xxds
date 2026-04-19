package com.linalg.linear_algebra_assistant.controller;

import com.linalg.linear_algebra_assistant.entity.User;
import com.linalg.linear_algebra_assistant.service.UserService;
import com.linalg.linear_algebra_assistant.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        boolean success = userService.register(user);
        Map<String, Object> res = new HashMap<>();
        if (success) {
            res.put("code", 200);
            res.put("message", "注册成功");
        } else {
            res.put("code", 400);
            res.put("message", "用户名已存在");
        }
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginUser) {
        User user = userService.findByUsername(loginUser.getUsername());
        if (user != null && new BCryptPasswordEncoder().matches(loginUser.getPassword(), user.getPassword())) {
            // 生成包含 userId 的 token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            Map<String, Object> res = new HashMap<>();
            res.put("code", 200);
            res.put("token", token);
            res.put("username", user.getUsername());
            res.put("role", user.getRole());
            return res;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("code", 401);
        res.put("message", "用户名或密码错误");
        return res;
    }
}