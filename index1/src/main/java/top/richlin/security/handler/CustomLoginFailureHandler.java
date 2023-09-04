package top.richlin.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import top.richlin.security.entity.UserLoginInfo;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomLoginFailureHandler
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 23:52
 * @description
 */
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //noinspection ReassignedVariable
        response = new ResponseTemplate(response)
                .failTemplate()
                .putInformation("msg","登录失败！")
                .putInformation("information", exception.getMessage())
                .build();
    }
}
