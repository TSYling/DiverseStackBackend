package top.richlin.security.entity;

import jakarta.websocket.Session;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocketConnectPool
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/1 23:38
 * 每个建立了webSocket连接的用户都要在这保存相关数据
 */
@Component
@Data
public class WebSocketConnectPool {
    /**
     * 静态变量，用来记录当前在线连接数，线程安全的类。
     */
    private static AtomicInteger onlineClientCount = new AtomicInteger(0);

    /**
     * 记录房间个数
     */
    private static AtomicInteger onlineRoomCount = new AtomicInteger(0);
    /**
     * 存放房间信息
     */
    @Autowired
    private static Rooms rooms;


    /**
     * 存放所有在线的客户端
     */
    private static Map<String, Session> onlineSessionClientMap = new ConcurrentHashMap<>();
}
