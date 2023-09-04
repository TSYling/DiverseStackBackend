package top.richlin.security.server;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocketHandler
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/31 16:51
 * 首先是基本的一系列功能
 */
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    /**
     * 静态变量，用来记录当前在线连接数，线程安全的类。
     */
    private static AtomicInteger onlineSessionClientCount = new AtomicInteger(0);

    /**
     * 存放所有在线的客户端
     */
    private static Map<String, WebSocketSession> onlineSessionClientMap = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        System.out.println(session.getPrincipal());
        onlineSessionClientCount.incrementAndGet();
        onlineSessionClientMap.put(session.getId(),session);
        broadcast();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        onlineSessionClientCount.decrementAndGet();
        onlineSessionClientMap.remove(session.getId());
    }
    public void sendTo(){

    }
    public void broadcast(){
        onlineSessionClientMap.forEach((uid,session)->{
            try {
                session.sendMessage(new TextMessage("加入了"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
