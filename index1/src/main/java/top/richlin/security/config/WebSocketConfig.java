package top.richlin.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import top.richlin.security.interceptor.CustomWebSocketServerInterceptor;
import top.richlin.security.server.WebSocketHandler;

/**
 * WebSocketConfig
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/30 19:31
 * @description
 */
//@Configuration
//public class WebSocketConfig{
//    @Bean
//    public ServerEndpointExporter serverEndpointExporter() {
//        ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter();
//        serverEndpointExporter.setAnnotatedEndpointClasses(CustomWebSocketServerHandler.class);
//        return serverEndpointExporter;
//    }
//}
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(),"/ws")
                .addInterceptors(new CustomWebSocketServerInterceptor())
                .setAllowedOrigins("*");
    }
}
