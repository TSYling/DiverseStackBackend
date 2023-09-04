package top.richlin.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;
import java.util.Objects;

/**
 * CustomAuthenticationEntryPointHandler
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 23:10
 * @description
 */
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response = new ResponseTemplate(response)
                .failTemplate()
                .putInformation("msg","未登录")
                .putInformation("detail",authException.getMessage())
                .build();
    }
}
