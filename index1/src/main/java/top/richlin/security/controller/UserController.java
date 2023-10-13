package top.richlin.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.service.UserService;
import top.richlin.security.service.impl.UserServiceImpl;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * UserController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 23:10
 * @description
 */
@RestController
@RequestMapping("/user/")
public class UserController {
    /**
     * 下一级经验等于等级*100
     */
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public void register(HttpServletResponse response,String username,String name,String password,String captcha,String invitation) throws IOException {
        userService.register(response,username,name,password,captcha,invitation);
    }
    @PostMapping("/info")
    public void info(HttpServletResponse response) throws IOException {
        userService.info(response);
    }
}
