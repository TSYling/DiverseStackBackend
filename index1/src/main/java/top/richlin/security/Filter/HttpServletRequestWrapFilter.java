package top.richlin.security.Filter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.filter.OncePerRequestFilter;
import top.richlin.security.template.ResponseTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServletRequestWrapFilter
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/18 0:49
 * @description 包装request 使其能够多次读取流数据
 */
public class HttpServletRequestWrapFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        log.info("request body servlet request wrap filter ...");
        if(request.getRequestURI().startsWith("/webSocket")){
            filterChain.doFilter(request,response);
            return;
        }
        final RequestBodyServletRequestWrapper requestWrapper = new RequestBodyServletRequestWrapper(request);
        Object jsonParseError = requestWrapper.getAttribute("jsonParseError");
        if(!ObjectUtil.isNull(jsonParseError)){
            response = new ResponseTemplate(response)
                    .failTemplate()
                    .putInformation("error",jsonParseError)
                    .build();
            return;
        }
        filterChain.doFilter(requestWrapper, response);
    }

    /**
     * ServletRequest 的包装器 (让后续方法可以重复调用 request.getInputStream())<br>
     * 处理流只能读取一次的问题, 用包装器继续将流写出. @RequestBody 会调用 getInputStream 方法, 所以本质上是解决 getInputStream 多次调用的问题: <br>
     * ServletRequest 的 getReader() 和 getInputStream() 两个方法只能被调用一次，而且不能两个都调用。那么如果 Filter 中调用了一次，在 Controller 里面就不能再调用了,
     * 会抛出异常:getReader() has already been called for this request 异常
     */
    private static class RequestBodyServletRequestWrapper extends HttpServletRequestWrapper {

        /**
         * 请求体数据
         */
        private final byte[] requestBody;
        /**
         * 重写的参数 Map
         */
        private final Map<String, String[]> paramMap;

        public RequestBodyServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            ServletInputStream inputStream = request.getInputStream();

            // 重写 requestBody
//            requestBody = IOUtils.toByteArray(request.getReader(), StandardCharsets.UTF_8);
            requestBody = inputStream.readAllBytes();

//            将requestBody的数据保存

//            System.out.println(new String(requestBody));
//            paramMap = new ObjectMapper().readValue(inputStream, Map.class);

            // 重写参数 Map
            paramMap = new HashMap<>();
            if (requestBody.length == 0) {
                return;
            }
//            paramMap = new ObjectMapper().readValue(requestBody, Map.class);
            try{
                JSON.parseObject(getRequestBody()).forEach((key, value) -> paramMap.put(key, new String[]{String.valueOf(value)}));
            }catch (Exception ignored){
                request.setAttribute("jsonParseError","参数格式不是json");
            }


        }

        public String getRequestBody() {
//            return StringUtils.toEncodedString(requestBody, StandardCharsets.UTF_8);
            return new String(requestBody);
        }


        // ~ get
        // -----------------------------------------------------------------------------------------------------------------

        @Override
        public Map<String, String[]> getParameterMap() {
            return paramMap;
        }

        @Override
        public String getParameter(String key) {
            String[] valueArr = paramMap.get(key);
            if (valueArr == null || valueArr.length == 0) {
                return null;
            }
            return valueArr[0];
        }

        @Override
        public String[] getParameterValues(String key) {
            return paramMap.get(key);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(paramMap.keySet());
        }

        // ~ read
        // -----------------------------------------------------------------------------------------------------------------

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(requestBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener listener) {

                }

                @Override
                public int read() {
                    return inputStream.read();
                }
            };
        }
    }
}
