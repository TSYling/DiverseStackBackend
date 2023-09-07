package top.richlin.security.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.StompConnectPool;
import top.richlin.security.entity.UserActiveState;
import top.richlin.security.util.MessageUtils;

/**
 * StompConnectEventListener
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/7 22:00
 * @description
 */
@Component
public class StompConnectEventListener implements ApplicationListener<SessionConnectedEvent> {
    private CustomUserDao userDao;
    private StompConnectPool stompConnectPool;

    @Autowired
    public StompConnectEventListener(CustomUserDao userDao, StompConnectPool stompConnectPool) {
        this.userDao = userDao;
        this.stompConnectPool = stompConnectPool;
    }

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        System.out.println("connect");
        Message<byte[]> message = event.getMessage();
        CustomUser user = MessageUtils.getCustomUser(message);
        /**
         * 不能直接改写message里的user信息 会引发错误
         */
        CustomUser newUser =userDao.loadByUsername(user.getUsername());
        newUser.setActiveStatus(UserActiveState.ONLINE);
        // 更新状态
        userDao.updateById(user);
        // 记录信息
        stompConnectPool.recordOnline(user);
    }
}
