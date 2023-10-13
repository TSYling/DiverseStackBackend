package top.richlin.security.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.StringUtils;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.handler.CustomLoginFailureHandler;
import top.richlin.security.handler.CustomLoginSuccessHandler;
import top.richlin.security.service.CustomUserDetailService;
import top.richlin.security.template.EmailTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LoginSuccessFilter
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/27 19:01
 * @description 记录用户上一次登录的时间
 */


public class LoginSuccessFilter extends UsernamePasswordAuthenticationFilter {
    private final CustomUserDao userDao;
    private final CustomUserDetailService userDetailService;


    public LoginSuccessFilter(CustomUserDao userDao, AuthenticationManager manager, CustomUserDetailService userDetailService) {
        super(manager);
        this.userDetailService = userDetailService;
        this.setFilterProcessesUrl("/user/login");
        this.setAuthenticationSuccessHandler(new CustomLoginSuccessHandler());
        this.setAuthenticationFailureHandler(new CustomLoginFailureHandler());
        this.userDao = userDao;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String code = request.getParameter("captcha");
        if(StringUtils.hasText(code)){
            code = code.toLowerCase().trim();
            String username = obtainUsername(request);
            CustomUser userDetails =(CustomUser) userDetailService.loadUserByUsername(username);
            checkCode(username,code);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            setDetails(request, authenticationToken);
            return authenticationToken;
        }
        else {
            return super.attemptAuthentication(request, response);
        }
    }
    private void checkCode(String email,String code){
        if(!EmailTemplate.checkVerifyCode(code,email)){
            throw new AuthenticationServiceException("验证码错误或已失效");
        }
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        // 加上自己定义的登录成功后的动作
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomUser user;
        // 若为匿名用户 则不记录最后登录时间
        if(!username.equals("anonymousUser")){
            user = userDao.loadById(username);
            user.setLastLoginTime(new Date());
        }
        else {
            return;
        }
        // 若为首次登录 需要将状态改为已激活 1
        if(user.getActiveStatus()==0){
            user.setActiveStatus(1);
        }
        userDao.updateById(user);
    }
}
