package top.richlin.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.Assert;

/**
 * CustomCsrfStrategy
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 22:56
 * @description
 */
public final class CustomCsrfStrategy implements SessionAuthenticationStrategy {
    private final Log logger = LogFactory.getLog(getClass());

    private final CsrfTokenRepository tokenRepository;

    private CsrfTokenRequestHandler requestHandler = new XorCsrfTokenRequestAttributeHandler();

    public CustomCsrfStrategy(CsrfTokenRepository tokenRepository) {
        Assert.notNull(tokenRepository, "tokenRepository cannot be null");
        this.tokenRepository = tokenRepository;
    }
    public void setRequestHandler(CsrfTokenRequestHandler requestHandler) {
        Assert.notNull(requestHandler, "requestHandler cannot be null");
        this.requestHandler = requestHandler;
    }
    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request,
                                 HttpServletResponse response) throws SessionAuthenticationException {
        if(!request.getMethod().equalsIgnoreCase("post")){
            this.tokenRepository.saveToken(this.tokenRepository.generateToken(request), request, response);
        }
    }
}
