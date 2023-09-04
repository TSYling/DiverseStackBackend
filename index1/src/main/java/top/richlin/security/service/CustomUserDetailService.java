package top.richlin.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;

import java.util.Objects;

/**
 * CustomUserDetailService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/29 14:23
 * @description
 */
@Component
public
class CustomUserDetailService implements UserDetailsService {

    private final CustomUserDao userDao;

    @Autowired
    public CustomUserDetailService(CustomUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUser customUser = userDao.loadByUsername(username);
        if (Objects.isNull(customUser)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return customUser;
    }
}
