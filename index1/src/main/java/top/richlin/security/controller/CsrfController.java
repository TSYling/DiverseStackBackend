package top.richlin.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.richlin.security.repository.CustomCookieCsrfTokenRepository;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;

/**
 * CsrfController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/30 16:26
 * @description
 */

@RestController
//@RequestMapping("/api")
public class CsrfController {
    CustomCookieCsrfTokenRepository customCookieCsrfTokenRepository = CustomCookieCsrfTokenRepository.withHttpOnlyFalse();
    @GetMapping("/csrf")
    public void getCsrf(HttpServletResponse response, HttpServletRequest request) throws IOException {
        customCookieCsrfTokenRepository.saveToken(customCookieCsrfTokenRepository.generateToken(request),request,response);
        response =new ResponseTemplate(response)
                .successTemplate()
                .build();
        response.flushBuffer();
    }
    @RequestMapping("ip")
    public String ip(HttpServletResponse response, HttpServletRequest request){
        System.out.println(request.toString());
        return request.getRemoteAddr() +" " +request.getHeader("X-Forwarded-For");
    }

}
