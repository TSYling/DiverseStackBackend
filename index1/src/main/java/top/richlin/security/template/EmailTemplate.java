package top.richlin.security.template;

import lombok.Data;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * EmailTemplate
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/28 14:16
 * @description 发送验证码的模板
 */
@Data
@Component
public class EmailTemplate {
    private static final String prefix = "verifyCode";
    private final SimpleMailMessage message = new SimpleMailMessage();
    private final JavaMailSenderImpl mailSender;
    private static RedisTemplate redisTemplate;
    @Autowired
    public EmailTemplate(JavaMailSenderImpl mailSender, RedisTemplate redisTemplate) {
        this.mailSender = mailSender;
        EmailTemplate.redisTemplate = redisTemplate;
        message.setFrom("1754350764@qq.com");
    }

    private String getRandomCode(int num){
        return UUID.randomUUID().toString().toLowerCase().substring(0, num);
    }
    private void storeCodeToRedis(String email,String code){
                redisTemplate.opsForValue().set(
                prefix +email,
                code,
                5,
                TimeUnit.MINUTES);
    }
    private static String getCodeFromRedis(String email){
        return (String) redisTemplate.opsForValue().get(prefix+email);
    }
    public void setTarget(String email){
        this.message.setTo(email);
    }
    public void setTitle(String title){
        this.message.setSubject(title);
    }
    public void setText(String text){
        this.message.setText(text);
    }
    public void setInfo(String email,String title,String text){
        this.setTarget(email);
        this.setTitle(title);
        this.setText(text);
    }
    public void loginTemplate(String email,String code){
        setInfo(
                email,
                "登录——验证码",
                "您正在进行登录操作，验证码为："+code.toUpperCase()+"\n验证码五分钟内有效请及时登录"
        );
    }
    public void registerTemplate(String email,String code){
        setInfo(
                email,
                "注册——验证码",
                "您正在进行注册操作，验证码为："+ code.toUpperCase() +"\n验证码5分钟内有效请及时注册"
        );
    }
    public void send(){
        Thread thread  = new Thread(()->{
            mailSender.send(message);
        });
        // 异步发送短信
        try{
            thread.start();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public boolean loginSend(String email){
        if(StringUtils.hasText(getCodeFromRedis(email))){
            // 若是已有验证码 就不再发送
            return false;
        }
        String code = getRandomCode(6);
        loginTemplate(email,code);
        storeCodeToRedis(email,code);
        send();
        return true;
    }
    public boolean registerSend(String email){
        if(StringUtils.hasText(getCodeFromRedis(email))){
            // 若是已有验证码 就不再发送
            return false;
        }
        String code = getRandomCode(6);
        registerTemplate(email,code);
        storeCodeToRedis(email,code);
        send();
        return true;
    }
    public static boolean checkEmail(String email){
        return EmailValidator.getInstance().isValid(email);
    }
    public static boolean checkVerifyCode(String code,String email){
        String realCode = getCodeFromRedis(email);
        if(StringUtils.hasText(realCode)){
            return code.equalsIgnoreCase(realCode);
        }
        return false;
    }

//    System.out.println(email);
//    // 校验邮箱
////        RegExpUtil.regExpVerify(RegExpUtil.emailRegExp, email, "邮箱格式错误");

}
