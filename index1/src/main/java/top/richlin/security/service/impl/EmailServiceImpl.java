package top.richlin.security.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.richlin.security.service.EmailService;
import top.richlin.security.template.EmailTemplate;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;

/**
 * EmailServiceImpl
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/28 15:42
 * @description
 */
@Service
public class EmailServiceImpl implements EmailService {
    private final EmailTemplate emailTemplate;

    @Autowired
    public EmailServiceImpl(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    @Override
    public void loginService(HttpServletResponse response,String email) throws IOException {
        if(!EmailTemplate.checkEmail(email)){
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","邮箱格式错误！")
                    .build();
            return;
        }
        boolean isSuccess = emailTemplate.loginSend(email);
        if(isSuccess){
            response = new ResponseTemplate(response)
                    .successTemplate()
                    .putInformation("msg","验证码发送成功！")
                    .build();
        }
        else {
            response = new ResponseTemplate(response)
                    .successTemplate()
                    .putInformation("msg","验证码已发送！")
                    .build();
        }
    }

    @Override
    public void registerService(HttpServletResponse response, String email) throws IOException {
        if(!EmailTemplate.checkEmail(email)){
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","邮箱格式错误！")
                    .build();
            return;
        }
        boolean isSuccess = emailTemplate.registerSend(email);
        if(isSuccess){
            response = new ResponseTemplate(response)
                    .successTemplate()
                    .putInformation("msg","验证码发送成功！")
                    .build();
        }
        else {
            response = new ResponseTemplate(response)
                    .successTemplate()
                    .putInformation("msg","验证码已发送！")
                    .build();
        }
    }
}
