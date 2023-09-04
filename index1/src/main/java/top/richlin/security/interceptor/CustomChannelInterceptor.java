package top.richlin.security.interceptor;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import top.richlin.security.entity.*;
import top.richlin.security.template.ResponseMessageTemplate;
import top.richlin.security.util.MessageUtils;

import java.util.List;

/**
 * CustomChannelInterceptor
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/2 23:02
 * 拦截请求
 */
@Component
public class CustomChannelInterceptor implements ChannelInterceptor {
    private final SimpMessagingTemplate template;
    private Rooms rooms;


    @Autowired
    @Lazy
    public CustomChannelInterceptor(SimpMessagingTemplate template,Rooms rooms) {
        this.template = template;
        this.rooms = rooms;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        /**
         * 此处用来对客户端  发送的请求进行拦截
         * 可以拦截连接请求 等其他发送 订阅请求
         * 标准内部源码为StompCommand.CONNECT.equals(command)
         * StompSubProtocolHandler
         * public void handleMessageToClient(WebSocketSession session, Message<?> message) {
         * 		if (!(message.getPayload() instanceof byte[] payload)) {
         * 			if (logger.isErrorEnabled()) {
         * 				logger.error("Expected byte[] payload. Ignoring " + message + ".");
         *                        }
         * 			return;* 		}
         *
         * 		StompHeaderAccessor accessor = getStompHeaderAccessor(message);
         * 		StompCommand command = accessor.getCommand();
         *
         * 		if (StompCommand.MESSAGE.equals(command)) {
         * 			if (accessor.getSubscriptionId() == null && logger.isWarnEnabled()) {
         * 				logger.warn("No STOMP \"subscription\" header in " + message);
         *            }
         * 		...
         */
        StompCommand command = accessor.getCommand();
        if (StompCommand.SUBSCRIBE.equals(command)) {
            return dealWithSubscribe(message,channel,accessor);
        }
        return message;
    }

    private Message<?> dealWithSubscribe(Message<?> message, MessageChannel channel, StompHeaderAccessor accessor) {

        String destination = accessor.getDestination();
        assert destination != null;
        /**
         * 状况反馈订阅接口放行
         */
        /**
         * 配置订阅失败规则
         * 1、用户为房间内被封（Banned）成员
         * 2、用户账号被封（不再考虑范围内） 禁止登录即可
         */
        // 判断是否为房间内被封禁成员
        /**
         * 获取发起订阅用户id
         * 获取订阅路径中房间id
         * 查找房间中封禁成员中是否包含用户
         */
        List<String> split = List.of(destination.split("/"));
        switch (split.get(1)) {
            case "room":
                // 进入房间订阅这个路径
                return dealWithRoomSubscribe(message,accessor,split.size()>2? split.get(2):"");
            default:
                return message;
        }
//        if (destination.equals("/user/topic/status")) {
//            return message;
//        }
    }
    private Message<?> dealWithRoomSubscribe(Message<?> message,StompHeaderAccessor accessor,String roomIdString){
        CustomUser user = MessageUtils.getCustomUser(message);
        int roomId = 0;
        if(StringUtils.isNumeric(roomIdString)){
            roomId = Integer.parseInt(roomIdString);
        }
        else {
            ResponseMessageTemplate.sendSubscribeFail(template,accessor,"无法获取房间");
            return null;
        }
        Room room = rooms.getSelectedRoom(roomId);
        if(ObjectUtil.isNull(room)){
            ResponseMessageTemplate.sendSubscribeFail(template,accessor,"房间不存在");
            return null;
        }
//        boolean isNotBanner = ObjectUtil.isNull(room.getBanedMembers().get(user.getId()));
//        if(!isNotBanner){
//            ResponseMessageTemplate.sendSubscribeFail(template,accessor,"您在封禁名单中");
//            return null;
//        }
        //  加入房间
        try{
            String password = MessageUtils.getHeaderString(message, "password");
            room.join(user,password);
            ResponseMessageTemplate.sendSubscribeSuccess(template,accessor,"");
            return message;
        }catch (Exception e){
            ResponseMessageTemplate.sendSubscribeFail(template,accessor,e.getMessage());
            return null;
        }
    }
}
