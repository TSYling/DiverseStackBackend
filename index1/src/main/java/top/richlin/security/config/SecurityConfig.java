package top.richlin.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.richlin.security.Filter.HttpServletRequestWrapFilter;
import top.richlin.security.Filter.LoginSuccessFilter;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.handler.CustomAccessDeniedHandler;
import top.richlin.security.handler.CustomAuthenticationEntryPointHandler;
import top.richlin.security.handler.CustomLoginSuccessHandler;
import top.richlin.security.repository.CustomCookieCsrfTokenRepository;
import top.richlin.security.service.CustomUserDetailService;
import top.richlin.security.strategy.CustomCsrfStrategy;
import top.richlin.security.template.ResponseTemplate;

import javax.sql.DataSource;
import java.util.*;

/**
 * SecurityConfig
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/15 22:03
 * @description springSecurity 配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final RedisIndexedSessionRepository sessionRepository;
    private final DataSource dataSource;
    private final CustomUserDetailService userDetailService;
    private final CustomUserDao userDao;
    private final AuthenticationConfiguration configuration;

    @Autowired
    public SecurityConfig(RedisIndexedSessionRepository sessionRepository, DataSource dataSource, CustomUserDetailService userDetailService, CustomUserDao userDao, AuthenticationConfiguration configuration) throws Exception {
        this.sessionRepository = sessionRepository;
        this.dataSource = dataSource;
        this.userDetailService = userDetailService;
        this.userDao = userDao;
        this.configuration = configuration;
    }
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 6.0版本以后WebSecurity 简单配置方法
        return (web) -> {
            web.ignoring().requestMatchers("/api/csrf");
//            web.ignoring().requestMatchers("/webSocket/*");
        };
    }
    @Bean
    public SecurityFilterChain Config(HttpSecurity http) throws Exception {

        //配置访问权限
        http.authorizeHttpRequests(request -> {
            request
                    .requestMatchers("/user/loginInfo").permitAll()
                    .requestMatchers("/user/register").permitAll()
                    .requestMatchers("/emailVerifyCode/**").permitAll()
                    .requestMatchers("/csrf").permitAll()
                    .requestMatchers("/ip").permitAll()
                    .requestMatchers("/error/**").permitAll()
                    .requestMatchers("/webSocket/*").permitAll()
//                    .requestMatchers("/**").permitAll()
//                    .anyRequest().permitAll()
                    .anyRequest().authenticated()
                    ;
        });
        //启用表单登录 此处不再使用是因为采用了新的认证方式 如果仍然启用它将会默认生成一个usernamePasswordAuthenticationFilter
//        http.formLogin(Customizer.withDefaults());
        //注销配置
        http.logout(logout->{
            logout.logoutUrl("/user/logout");
            logout.logoutSuccessHandler((request, response, authentication) -> {
                response =new ResponseTemplate(response)
                       .successTemplate()
                       .putInformation("msg","注销成功！")
                       .build();
                response.flushBuffer();
            });
                //清除相关信息
            logout.invalidateHttpSession(true).clearAuthentication(true);
        });

        //csrf
        http.csrf(csrf->{
            csrf.csrfTokenRepository(CustomCookieCsrfTokenRepository.withHttpOnlyFalse()); //将令牌保存到cookie中
            csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
            csrf.sessionAuthenticationStrategy(new CustomCsrfStrategy(CustomCookieCsrfTokenRepository.withHttpOnlyFalse()));
            csrf.ignoringRequestMatchers("/csrf");
            csrf.ignoringRequestMatchers("/webSocket/**");
//            csrf.disable();
        });
        //异常处理
        http.exceptionHandling(handlingConfigurer -> {
            /**
             * 2023-09-05T16:13:04.890+08:00 ERROR 16516 --- [nio-8080-exec-5] o.a.c.c.C.[Tomcat].[localhost]
             * : Exception Processing ErrorPage[errorCode=0, location=/error]
             * springMVC 引发错误信息默认错误页面为/error 配置/error页面通过将不会显示access denied
             */
            handlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler());
            handlingConfigurer.authenticationEntryPoint(new CustomAuthenticationEntryPointHandler());
        });
        // Core跨域
//        http.cors(cors->{
//           cors.configurationSource(configurationSource());
//        });
        // session 管理
        http.sessionManagement(session->{
           session

                   .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                   .maximumSessions(1)
//                   .expiredUrl("/sessionExpire")
                   .expiredSessionStrategy(event -> {
                       HttpServletResponse response = event.getResponse();
                       response.setContentType("application/json;charset=UTF-8");
                       response = new ResponseTemplate(response)
                               .failTemplate()
                               .putInformation("msg","会话已失效")
                               .build();
                       response.flushBuffer();
                   })
                   .sessionRegistry(registry())
//                   .maxSessionsPreventsLogin(true)// 防止再次登录
           ;
        });
        //remember me 还是得用 认证过程中需要使用
        http.rememberMe(rememberMe->{
            rememberMe.rememberMeServices(rememberMeServices());
            rememberMe.disable();
        });

        //CustomFilter
        http.addFilterBefore(new HttpServletRequestWrapFilter(), CsrfFilter.class);
        http.addFilterAt(loginSuccessFilter(), UsernamePasswordAuthenticationFilter.class);
        DefaultSecurityFilterChain build = http.build();
        // 为了获取默认的session 策略并且设置其他默认配置
        /**
         * 踩雷注意
         * 如果使用自定义过滤器继承自usernamePasswordAuthenticationFilter 默认继承后的配置与默认设置configuration有所不同
         * 具体需要注意的有如下三个方面
         * 1.SessionAuthenticationStrategy
         *  如果不重新配置 会导致上文中http.sessionManagement(session->{
         *            session
         *            ...}
         *            配置失效 表现为不产生session 若是前面配置了sessionManagement的检测
         *            例如：sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
         *            这个将开启对request的session认证检测 从而导致结果为不产生session
         *            但是又对session有检测判断 从而破环产生-使用的闭环而导致服务失效
         * 2.RememberMeServices
         *  如果不重新配置 会导致上文中配置失效
         *      从而导致rememberMe 不会返回cookie数据
         *      而上面的配置可以使得rememberMe的请求被检测 从而破坏闭环导致rememberMe服务失效
         * 3.SecurityContextRepository
         *  此项配置是为了统一获取认证信息，如果配置不同将导致的结果有
         *  登录一个账号之后未退出后再次登录一个账号返回的登录认证信息符合预期 但是获取用户数据的信息不同
         *  原因在于 默认的SecurityContextRepository为 RequestAttributeSecurityContextRepository
         *  通过检测发送请求的请求头要求字段查询信息，但是此处依然使用session获取登录信息从而导致错误。
         */
        loginSuccessFilter().setSessionAuthenticationStrategy(http.getSharedObject(SessionAuthenticationStrategy.class));
        loginSuccessFilter().setRememberMeServices(rememberMeServices());
        loginSuccessFilter().setSecurityContextRepository(new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository()));
        return build;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedOrigins(List.of("*"));
//        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        return source;
    }
    @Bean
    public SpringSessionBackedSessionRegistry registry(){
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
    @Bean
    public RememberMeServices rememberMeServices() {
////        rememberMe 可以将数据存储导内存或者数据库 但是数据库的话就需要配置表结构
//        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
//        tokenRepository.setDataSource(dataSource);
////        tokenRepository.setCreateTableOnStartup(true); // 启动时创建表结构
//        return new PersistentTokenBasedRememberMeServices(UUID.randomUUID().toString(),userDetailService, tokenRepository);
//        前后端分离
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
////        tokenRepository.setCreateTableOnStartup(true); // 启动时创建表结构
        CustomPersistentTokenBaseRememberMeService customPersistentTokenBaseRememberMeService = new CustomPersistentTokenBaseRememberMeService(UUID.randomUUID().toString(), userDetailService, tokenRepository);
        customPersistentTokenBaseRememberMeService.setAlwaysRemember(true);
        // 配置remember-meToken生效15天
        customPersistentTokenBaseRememberMeService.setTokenValiditySeconds(60*60*24*15);
        return customPersistentTokenBaseRememberMeService;
    }
    @Bean
    public LoginSuccessFilter loginSuccessFilter() throws Exception {
        return new LoginSuccessFilter(
                userDao,configuration.getAuthenticationManager(),userDetailService
        );
    }
}



@Setter
class CustomPersistentTokenBaseRememberMeService extends PersistentTokenBasedRememberMeServices {
    private boolean alwaysRemember;

    public CustomPersistentTokenBaseRememberMeService(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        if(this.alwaysRemember){
            return true;
        }
        String paramValue = request.getAttribute(parameter).toString();
        if (paramValue != null) {
            if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
                    || paramValue.equalsIgnoreCase("yes") || paramValue.equals("1")) {
                return true;
            }
        }
        this.logger.debug(
                LogMessage.format("Did not send remember-me cookie (principal did not set parameter '%s')", parameter));
        return false;
    }
}
