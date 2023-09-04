package top.richlin.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;

/**
 * CustomAccessDeniedHandler
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 23:10
 * @description
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String reason;
        if(accessDeniedException instanceof InvalidCsrfTokenException){
            reason = "无效CSRF TOKEN";
        }
        else if(accessDeniedException instanceof MissingCsrfTokenException){
            reason = "缺失CSRF TOKEN";
        }
        else {
            //可以代表权限不足
            reason = accessDeniedException.getMessage();
        }
        //noinspection ReassignedVariable
        response = new ResponseTemplate(response)
                .failTemplate()
                .putInformation("msg","不可访问")
                .putInformation("reason",reason)
                .build();
    }
}
