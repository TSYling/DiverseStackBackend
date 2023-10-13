package top.richlin.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import top.richlin.security.Filter.LoginSuccessFilter;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CustomUserDetailService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/29 14:23
 * @description
 */
@Component
public class CustomUserDetailService implements UserDetailsService {

    private final CustomUserDao userDao;

    @Autowired
    public CustomUserDetailService(CustomUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        username = (username != null) ? username.trim() : "";
        CustomUser customUser = obtainUser(username);
        if (Objects.isNull(customUser)) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return customUser;
    }
    private CustomUser obtainUser(String username){
        int type = AccountCheck.check(username);
        return switch (type) {
            case 1 -> userDao.loadByPhone(username);
            case 2 -> userDao.loadByEmail(username);
            case 3 -> userDao.loadById(username.substring(1));
            default -> null;
        };
    }
    public CustomUser createUser(String username) throws RuntimeException{
        int type = AccountCheck.check(username);
        CustomUser user = new CustomUser();
        switch (type) {
            case 1 -> user.setPhone(username);
            case 2 -> user.setEmail(username);
            default -> throw new RuntimeException("手机或邮箱格式错误");
        };
        return user;
    }
    private static class AccountCheck{
        public static final int INVALID_TYPE = 0;
        public static final int PHONE_TYPE = 1;
        public static final int EMAIL_TYPE = 2;
        public static int ID_TYPE = 3;
        private static final String phonePattern = "1\\d{10}";
        private static final String emailPattern = "\\S+@\\S+\\.\\S";
        private static final String idPattern = "^i[1-9]\\d*";
        private static final Pattern phoneRegex = Pattern.compile(phonePattern);
        private static final Pattern emailRegex = Pattern.compile(emailPattern);
        private static final Pattern idRegex = Pattern.compile(idPattern);
        public static int check(String username){
            Matcher phoneMatcher = phoneRegex.matcher(username);
            Matcher emailMatcher = emailRegex.matcher(username);
            Matcher idMatcher = idRegex.matcher(username);
            if(phoneMatcher.find())
                return PHONE_TYPE;
            else if(emailMatcher.find())
                return EMAIL_TYPE;
            else if(idMatcher.find())
                return ID_TYPE;
            else
                return INVALID_TYPE;
        }
    }
}
