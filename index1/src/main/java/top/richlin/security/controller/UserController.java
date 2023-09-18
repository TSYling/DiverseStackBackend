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
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseBody
    public void register(HttpServletResponse response, registerForm userForm) throws IOException {
        /**
         * 用户注册
         */
        CustomUser user = new CustomUser();
        user.setEmail(userForm.getUsername());
        user.setPassword(userForm.getPassword());
        userService.register(response,user,userForm.getCode());
    }
    @PostMapping("/info")
    public void info(HttpServletResponse response) throws IOException {
        userService.info(response);
    }
}
@Data
class registerForm{
    private String username;
    private String password;
    private String code;
}
