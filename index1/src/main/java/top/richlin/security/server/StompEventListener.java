package top.richlin.security.server;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.StompConnectPool;
import top.richlin.security.entity.UserActiveState;
import top.richlin.security.util.MessageUtils;

import java.util.*;

/**
 * StompConnectEventListener
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/7 22:00
 * @description
 */
@Component
public class StompEventListener {
    private final CustomUserDao userDao;
    private final StompConnectPool stompConnectPool;

    @Autowired
    public StompEventListener(CustomUserDao userDao, StompConnectPool stompConnectPool) {
        this.userDao = userDao;
        this.stompConnectPool = stompConnectPool;
    }
    @EventListener
    public void onConnect(SessionConnectedEvent event){
        Message<byte[]> message = event.getMessage();
        CustomUser user = MessageUtils.getCustomUser(message);
        /**
         * 不能直接改写message里的user信息 会引发错误
         */
        CustomUser newUser =userDao.loadById(user.getUsername());
        newUser.setActiveStatus(UserActiveState.ONLINE);
        // 更新状态
        userDao.updateById(user);
        // 记录信息
        stompConnectPool.recordOnline(user,message);
    }

    /**
     * 将订阅Id记录下来 
     */
    @EventListener
    public void onSubscribe(SessionSubscribeEvent event){
        Message<byte[]> message = event.getMessage();
        MessageHeaders headers = message.getHeaders();
        String subscriptionId = SimpMessageHeaderAccessor.getSubscriptionId(headers);
        String destination = SimpMessageHeaderAccessor.getDestination(headers);
        Integer userId = MessageUtils.getCustomUser(message).getId();
        Map<String, String> subInfo = stompConnectPool.onlineClientSubscribeInfo.get(userId);
        if(ObjectUtil.isNull(subInfo)){
            subInfo = new HashMap<>();
            subInfo.put(subscriptionId,destination);
            stompConnectPool.onlineClientSubscribeInfo.putIfAbsent(userId,subInfo);
        }else{
            subInfo.put(subscriptionId,destination);
            System.out.println(subInfo);
        }
    }

    /**
     * 取消订阅的将其从记录中剔除 
     */
    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event){
        Message<byte[]> message = event.getMessage();
        MessageHeaders headers = message.getHeaders();
        String subscriptionId = SimpMessageHeaderAccessor.getSubscriptionId(headers);
        Integer userId = MessageUtils.getCustomUser(message).getId();
        Map<String, String> subInfo = stompConnectPool.onlineClientSubscribeInfo.get(userId);
        subInfo.remove(subscriptionId);
    }

}
