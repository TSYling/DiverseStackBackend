package top.richlin.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import top.richlin.security.entity.UserLoginInfo;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomLoginSuccessHandler
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 23:52
 * @description
 */
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        //noinspection ReassignedVariable
        response = new ResponseTemplate(response)
                .successTemplate()
                .putInformation("msg","登录成功！")
                .putInformation("authentication", new UserLoginInfo((UsernamePasswordAuthenticationToken) authentication).info())
                .build();
    }
}
