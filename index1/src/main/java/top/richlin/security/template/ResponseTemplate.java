package top.richlin.security.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import top.richlin.security.entity.UserLoginInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * ResponseTemplate
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/16 21:18
 * @description
 */
@Component
public class ResponseTemplate {
    HttpServletResponse response;
    Map<String,Object> information;
    public ResponseTemplate(HttpServletResponse response){
        this.response = response;
        this.information = new HashMap<>();
    }
    public ResponseTemplate successTemplate() throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        information.put("status",HttpStatus.OK.value());
        return this;
    }
    public ResponseTemplate failTemplate() throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        information.put("status",HttpStatus.FORBIDDEN.value());
        return this;
    }
    public ResponseTemplate setHttpStatus(int hs){
        response.setStatus(hs);
        information.put("status",hs);
        return this;
    }

    public ResponseTemplate putInformation(String K,Object V){
        information.put(K,V);
        return this;
    }
    public HttpServletResponse build() throws IOException {
        String s = new ObjectMapper().writeValueAsString(information);
        response.getWriter().write(s);
        return response;
    }
}
