package top.richlin.security.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.richlin.security.service.EmailService;

import java.io.IOException;

/**
 * EmailController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/28 15:39
 * @description
 */
@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public void loginSend(HttpServletResponse response, @RequestParam("username") String email) throws IOException {
        emailService.loginService(response,email);
        response.flushBuffer();
    }
    @PostMapping("/register")
    public void register(HttpServletResponse response, @RequestParam("username") String email) throws IOException {
        emailService.registerService(response,email);
        response.flushBuffer();
    }
}
