package top.richlin.security.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.druid.sql.dialect.odps.ast.OdpsObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import top.richlin.security.entity.CustomUser;
import top.richlin.security.entity.ResponseMessage;
import top.richlin.security.entity.Rooms;
import top.richlin.security.template.ResponseMessageTemplate;
import top.richlin.security.util.MessageUtils;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocketController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/30 19:29
 * @description
 */
@RestController
public class WebSocketController {
    @Autowired
    WebSocketMessageBrokerStats stats;
    @MessageMapping("/sendTest")
    @SendTo("/topic/subscribeTest")
    public String sendDemo(Message message) {
//        System.out.println(d);
        System.out.println(stats);
        MessageHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message);
        MessageHeaders messageHeaders = accessor.getMessageHeaders();
        Object simpUser = messageHeaders.get("simpUser");
        if(!ObjectUtils.isEmpty(simpUser)){
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)simpUser;
            CustomUser principal = (CustomUser) token.getPrincipal();
            System.out.println(principal);
            System.out.println(principal.getEmail());
        }
        return "1";
    }

    @SubscribeMapping("/subscribeTest")
    public String sub() {
        System.out.println("subscribe");
        return "subscribe";
    }
    @Autowired
    private SimpMessagingTemplate template;

    //广播推送消息
//    @Scheduled(fixedRate = 10000)
    public void sendTopicMessage() {
        System.out.println("后台广播推送！");
        this.template.convertAndSend("/topic/test","123123");
    }

    @MessageMapping("/createRoom/{password}")
    @SendToUser("/topic/status")
    /**
     * !important 注意注意  这边 开头的/Topic 必须要为设置中已经设置过的   否者subscribe无效
     */
    public ResponseMessage createRoom(Message message,@DestinationVariable String password){
        CustomUser user = MessageUtils.getCustomUser(message);
        if(ObjectUtil.isNull(user)){
            return ResponseMessageTemplate.getFailResponseMessage("error","用户未登录");
        }
        int roomId = 0;
        if(StringUtils.isEmpty(password)||!StringUtils.isAlphanumeric(password)){
            // 判断输入的账号密码是否为数字和字母组成 不是则忽略
            roomId = Rooms.createRoom(user);
        }
        else{
            roomId = Rooms.createRoomWithPassword(user,password);
        }

        return ResponseMessageTemplate.getSuccessResponseMessage("roomId",roomId);
    }
    @MessageMapping("/joinRoom")
    @SendToUser("/topic/status")
    public ResponseMessage joinRoom(Message message,@Payload String roomId){
        /**
         * 用户在非禁止下才可加入房间
         */
        return null;
    }
}
