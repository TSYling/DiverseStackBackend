package top.richlin.security.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import top.richlin.security.entity.CustomUser;

import java.io.IOException;

/**
 * UserService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/27 22:51
 * @description
 */
@Service
public interface UserService {
    public void register(HttpServletResponse response,String username,String name,String password,String captcha,String invitation) throws IOException;

    void info(HttpServletResponse response) throws IOException;
}
