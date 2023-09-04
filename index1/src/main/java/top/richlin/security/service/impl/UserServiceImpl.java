package top.richlin.security.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.UserInfo;
import top.richlin.security.service.UserService;
import top.richlin.security.template.EmailTemplate;
import top.richlin.security.template.ResponseTemplate;

import java.io.IOException;
import java.util.Date;

/**
 * UserServiceImpl
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/27 22:53
 * @description
 */
@Service
public class UserServiceImpl implements UserService {
    private CustomUserDao userDao;

    @Autowired
    public UserServiceImpl(CustomUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void register(HttpServletResponse response, CustomUser user,String code) throws IOException {
        // 对数据校验是否符合规则
        if(!StringUtils.hasText(user.getUsername())||!StringUtils.hasText(user.getPassword())||!StringUtils.hasText(code)
                ||user.getPassword().length()<6||user.getPassword().length()>20){
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","数据验证失败！")
                    .build();
            return;
        }
        boolean isUserExist = ObjectUtil.isNotNull(userDao.loadByUsername(user.getUsername()));
        if(isUserExist){
            // 已经有用户存在时
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","用户已存在！")
                    .build();
        }
        else{
            // 用户还未注册
            // 首先验证邮箱验证码
            if(!EmailTemplate.checkVerifyCode(code, user.getEmail())){
                // 验证失败报错退出
                response = new ResponseTemplate(response)
                        .failTemplate()
                        .putInformation("msg","验证码失效或错误！")
                        .build();
                return;
            }
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            user.setCoin(0);
            user.setRegisterTime(new Date());
            int num = userDao.insert(user);
            // num表示成功的条数
            if(num!=0){
                response = new ResponseTemplate(response)
                        .successTemplate()
                        .putInformation("msg","注册成功！")
                        .putInformation("data",user.getUsername())
                        .build();
            }else{
                response = new ResponseTemplate(response)
                        .failTemplate()
                        .setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .putInformation("msg","服务器错误！")
                        .build();
            }
        }
    }

    @Override
    public void info(HttpServletResponse response) throws IOException {
        // 获取email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // 通过email 获取用户相关信息
        UserInfo info = userDao.getInfoByUsername(email);
        if(ObjectUtil.isNull(info)){
            response=new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg","查询出错！")
                    .build();
            return;
        }
        // 需要对数据进行简单处理
        // 电话号码保密处理
        String phone = info.getPhone();
        if(phone!=null){
            phone = new StringBuffer(phone).replace(3,7,"*".repeat(4)).toString();
            info.setPhone(phone);
        }
        // activeStatus 处理
        int activeStatus = info.getActiveStatus();
        String activeStatusString = switch (activeStatus) {
            case 0 -> "inactive";
            case 1 -> "active";
            case 2 -> "frozen";
            case 3 -> "banned";
            case 4 -> "banish";
            case 5 -> "online";
            default -> "error";
        };
        info.setActiveStatusString(activeStatusString);
        // gender 处理
        String gender = info.getGender();
        if(gender.equalsIgnoreCase("u")){
            gender = "未知";
        } else if (gender.equalsIgnoreCase("w")) {
            gender = "女";
        }
        else if(gender.equalsIgnoreCase("m")){
            gender = "男";
        }
        else {
            gender = "其他";
        }
        info.setGender(gender);
        response = new ResponseTemplate(response)
                .successTemplate()
                .putInformation("msg","成功")
                .putInformation("data",info)
                .build();
        response.flushBuffer();
    }
}
