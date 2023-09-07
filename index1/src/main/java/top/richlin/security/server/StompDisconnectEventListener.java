package top.richlin.security.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.StompConnectPool;
import top.richlin.security.entity.UserActiveState;
import top.richlin.security.util.MessageUtils;

/**
 * StompDisconnectEventListener
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/5 13:35
 * @description
 */
@Component
public class StompDisconnectEventListener implements ApplicationListener<SessionDisconnectEvent> {
    private final CustomUserDao userDao;
    private final StompConnectPool stompConnectPool;

    @Autowired
    public StompDisconnectEventListener(CustomUserDao userDao, StompConnectPool stompConnectPool) {
        this.userDao = userDao;
        this.stompConnectPool = stompConnectPool;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        /**
         * 这里可以对用户断线后进行操作
         * 1、用户加入的房间都要退出
         * 2、设置用户不在线 ps:目前还是用mysql中记录用户在线状态，频繁登录退出操作可能负担较大
         */
        Message<byte[]> message = event.getMessage();
        CustomUser user = MessageUtils.getCustomUser(message);
        // 退出用户加入的房间
        stompConnectPool.leaveAllRoom(user);
        user.setActiveStatus(UserActiveState.OFFLINE);
        // 更新状态
        userDao.updateById(user);
        // 记录离线
        stompConnectPool.recordOffline(user);
    }
}
