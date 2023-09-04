package top.richlin.security.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * CustomCookieCsrfTokenRepository
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 22:46
 * @description
 */
public class CustomCookieCsrfTokenRepository implements CsrfTokenRepository {
    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";

    static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    private static final String CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME = CustomCookieCsrfTokenRepository.class.getName()
            .concat(".REMOVED");

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

    private String headerName = DEFAULT_CSRF_HEADER_NAME;

    private String cookieName = DEFAULT_CSRF_COOKIE_NAME;

    private boolean cookieHttpOnly = true;

    private String cookiePath;

    private String cookieDomain;

    private Boolean secure;

    private int cookieMaxAge = -1;

    private Consumer<ResponseCookie.ResponseCookieBuilder> cookieCustomizer = (builder) -> {
    };


    public void setCookieCustomizer(Consumer<ResponseCookie.ResponseCookieBuilder> cookieCustomizer) {
        Assert.notNull(cookieCustomizer, "cookieCustomizer must not be null");
        this.cookieCustomizer = cookieCustomizer;
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(this.headerName, this.parameterName, createNewToken());
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = (token != null) ? token.getToken() : "";

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(this.cookieName, tokenValue)
                .secure((this.secure != null) ? this.secure : request.isSecure())
                .path(StringUtils.hasLength(this.cookiePath) ? this.cookiePath : this.getRequestContext(request))
                .maxAge((token != null) ? this.cookieMaxAge : 0).httpOnly(this.cookieHttpOnly)
                .domain(this.cookieDomain);

        this.cookieCustomizer.accept(cookieBuilder);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());

        // Set request attribute to signal that response has blank cookie value,
        // which allows loadToken to return null when token has been removed
        if (!StringUtils.hasLength(tokenValue)) {
            request.setAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME, Boolean.TRUE);
        }
        else {
            request.removeAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        // Return null when token has been removed during the current request
        // which allows loadDeferredToken to re-generate the token
        if (Boolean.TRUE.equals(request.getAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME))) {
            return null;
        }
        Cookie cookie = WebUtils.getCookie(request, this.cookieName);
        if (cookie == null) {
            return null;
        }
        String token = cookie.getValue();
        if (!StringUtils.hasLength(token)) {
            return null;
        }
        return new DefaultCsrfToken(this.headerName, this.parameterName, token);
    }

    public void setParameterName(String parameterName) {
        Assert.notNull(parameterName, "parameterName cannot be null");
        this.parameterName = parameterName;
    }


    public void setHeaderName(String headerName) {
        Assert.notNull(headerName, "headerName cannot be null");
        this.headerName = headerName;
    }

    public void setCookieName(String cookieName) {
        Assert.notNull(cookieName, "cookieName cannot be null");
        this.cookieName = cookieName;
    }


    private String getRequestContext(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (contextPath.length() > 0) ? contextPath : "/";
    }

    @Bean
    public static CustomCookieCsrfTokenRepository withHttpOnlyFalse() {
        CustomCookieCsrfTokenRepository result = new CustomCookieCsrfTokenRepository();
        result.setCookieCustomizer((cookie) -> cookie.httpOnly(false));
        return result;
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }

    public void setCookiePath(String path) {
        this.cookiePath = path;
    }

    public String getCookiePath() {
        return this.cookiePath;
    }

    @Override
    public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
        return new MyRepositoryDeferredCsrfToken(this,request,response);
    }
}
final class MyRepositoryDeferredCsrfToken implements DeferredCsrfToken {

    private final CsrfTokenRepository csrfTokenRepository;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private CsrfToken csrfToken;

    private boolean missingToken;

    MyRepositoryDeferredCsrfToken(CsrfTokenRepository csrfTokenRepository, HttpServletRequest request,
                                  HttpServletResponse response) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.request = request;
        this.response = response;
    }

    @Override
    public CsrfToken get() {
        init();
        return this.csrfToken;
    }

    @Override
    public boolean isGenerated() {
        init();
        return this.missingToken;
    }

    private void init() {
        if (this.csrfToken != null) {
            return;
        }
        // 有没有都要新建
        this.csrfToken = this.csrfTokenRepository.loadToken(this.request);
        this.missingToken = (this.csrfToken == null);
        if (this.missingToken) {
            this.csrfToken = this.csrfTokenRepository.generateToken(this.request);
            this.csrfTokenRepository.saveToken(this.csrfToken, this.request, this.response);
        }else {
            this.csrfTokenRepository.saveToken(this.csrfTokenRepository.generateToken(this.request), this.request, this.response);
        }
    }

}