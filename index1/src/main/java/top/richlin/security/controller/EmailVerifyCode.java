package top.richlin.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.richlin.security.service.EmailService;
import top.richlin.security.service.impl.EmailServiceImpl;

import java.io.IOException;

/**
 * EmailVerifyCode
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/28 15:39
 * @description
 */
@RestController
@RequestMapping("/emailVerifyCode")
public class EmailVerifyCode {
    private final EmailService emailService;

    @Autowired
    public EmailVerifyCode(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public void loginSend(HttpServletResponse response, @RequestParam("email") String email) throws IOException {
        emailService.loginService(response,email);
        response.flushBuffer();
    }
    @PostMapping("/register")
    public void register(HttpServletResponse response, @RequestParam("email") String email) throws IOException {
        emailService.registerService(response,email);
        response.flushBuffer();
    }
}
