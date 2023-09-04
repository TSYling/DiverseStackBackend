package top.richlin.security.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * EmailService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/28 15:42
 * @description
 */
public interface EmailService {
    public void loginService(HttpServletResponse response, String email) throws IOException;

    public void registerService(HttpServletResponse response, String email) throws IOException;
}
