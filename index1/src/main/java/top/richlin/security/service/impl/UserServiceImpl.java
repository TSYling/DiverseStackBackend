package top.richlin.security.service.impl;

import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.UserInfo;
import top.richlin.security.service.CustomUserDetailService;
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
    private final CustomUserDao userDao;
    private final CustomUserDetailService detailService;

    @Autowired
    public UserServiceImpl(CustomUserDao userDao, CustomUserDetailService detailService) {
        this.userDao = userDao;
        this.detailService = detailService;
    }

    /**
     * 采用默认级别事务方式，为的是防止获取last_insert_id引发并发冲突
     * 默认级别对insert锁 read不锁 不大影响数据库性能
     * SERIALIZABLE 模式则是事务串行执行，会影响性能
     * @Transactional 执行事务
     */
    @Transactional
    @Override
    public void register(HttpServletResponse response, String username, String name, String password, String captcha, String invitation) throws IOException {
        // 对数据校验是否符合规则
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)
                || password.length() < 6 || password.length() > 20) {
            // 共同数据的校验
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg", "数据验证失败！")
                    .build();
            return;
        }
        // 使用验证码的注册
        try {
            CustomUser user;
            if (StringUtils.hasText(username) && StringUtils.hasText(captcha)) {
                user = registerByCaptcha(username, name, password, captcha);
            }
            // 使用邀请码的注册
            else if (StringUtils.hasText(invitation)) {
                user = registerByInvitation(name,password,invitation);
            } else{
                // 校验失败
                response = new ResponseTemplate(response)
                        .failTemplate()
                        .putInformation("msg", "数据验证失败！")
                        .build();
                return;
            }
            if(ObjectUtil.isNull(user)){
                response = new ResponseTemplate(response)
                        .failTemplate()
                        .putInformation("msg", "发生了百年难得一遇的错误")
                        .build();
                return;
            }
            int num = userDao.insert(user);
            // num表示成功的条数
            if (num != 0) {
                int insertId = userDao.lastInsertId();
                response = new ResponseTemplate(response)
                        .successTemplate()
                        .putInformation("msg", "注册成功！")
                        .putInformation("data", insertId)
                        .build();
            } else {
                response = new ResponseTemplate(response)
                        .failTemplate()
                        .setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .putInformation("msg", "服务器错误！")
                        .build();
            }
        } catch (Exception e) {
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg", e.getMessage())
                    .build();
        }
    }

    @Override
    public void info(HttpServletResponse response) throws IOException {
        // 获取username
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        // 通过email 获取用户相关信息
        UserInfo info = userDao.getInfoByUsername(id);
        if (ObjectUtil.isNull(info)) {
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("msg", "查询出错！")
                    .build();
            return;
        }
        // 需要对数据进行简单处理
        // 电话号码保密处理
        String phone = info.getPhone();
        if (phone != null) {
            phone = new StringBuffer(phone).replace(3, 7, "*".repeat(4)).toString();
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
        if (gender.equalsIgnoreCase("u")) {
            gender = "未知";
        } else if (gender.equalsIgnoreCase("w")) {
            gender = "女";
        } else if (gender.equalsIgnoreCase("m")) {
            gender = "男";
        } else {
            gender = "其他";
        }
        info.setGender(gender);
        response = new ResponseTemplate(response)
                .successTemplate()
                .putInformation("msg", "成功")
                .putInformation("data", info)
                .build();
        response.flushBuffer();
    }

    private CustomUser registerByCaptcha(String username, String name, String password, String captcha) throws RuntimeException {
        boolean isUserExist = true;
        try{
            // 当没找到用户的时候会抛出异常
            detailService.loadUserByUsername(username);
        }catch (Exception e){
            isUserExist = false;
        }
        if (isUserExist) {
            // 已经有用户存在时
            throw new RuntimeException("用户已注册");
        } else {
            // 用户还未注册
            // 首先验证验证码
            CustomUser user = detailService.createUser(username);
            if (!EmailTemplate.checkVerifyCode(captcha, username)) {
                // 验证失败报错退出
                throw new RuntimeException("验证码错误或已失效");
            }
            user.setName(name);
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            user.setCoin(0);
            user.setRegisterTime(new Date());
            return user;
        }
    }

    CustomUser registerByInvitation(String name, String password, String invitation) {
        // TODO 邀请码注册福利待定
        if(!invitation.equals("test-only"))
            throw new RuntimeException("邀请码不存在");
        CustomUser user = new CustomUser();
        user.setName(name);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setCoin(8888);
        user.setRegisterTime(new Date());
        user.setInviteCode(invitation);
        return user;
    }
}
