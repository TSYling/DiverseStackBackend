package top.richlin.security.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.socket.WebSocketMessage;
import top.richlin.security.entity.CustomUser;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MessageUtils
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/31 18:30
 * 处理webSocket的消息
 */
public class MessageUtils {
    // 房间 加入
    public static String MESSAGE_TYPE_JOIN = "join";
    // 房间 离开
    public static String MESSAGE_TYPE_LEAVE = "leave";
    // 房间 创建
    public static String MESSAGE_TYPE_CREATE = "create";
    // 私聊
    public static String MESSAGE_TYPE_CHAT = "chat";
    //
//    public static String MESSAGE_TYPE_JOIN = "join";
//    public static String MESSAGE_TYPE_JOIN = "join";
//    public static String MESSAGE_TYPE_JOIN = "join";

    /**
     *
     * @param message
     * @return message.type
     * 获取message的type
     */
    public static String type(Message<?> message){
        return JSONUtil.parseObj(message.getPayload()).getStr("type");
    }
    /**
     *
     * @param message
     * @return message.message
     * 获取message的message
     */
    public static String msg(Message<?> message){
        return JSONUtil.parseObj(message.getPayload()).getStr("message");
    }
    public static String get(Message<?> message,String key){
        try{
            return JSONUtil.parseObj(message.getPayload()).getStr(key);
        }catch (Exception e){
            return null;
        }

    }
    public static String getHeaderString(Message<?> message,String key){
        try{
            MessageHeaders headers = message.getHeaders();
            Map<String,Object> headerMap = (Map<String,Object>)headers.get("nativeHeaders");
            ArrayList<Object> values =(ArrayList<Object>) headerMap.get(key);
            List<String> stringValues = values.stream().map(value->{
                try {
                    return new ObjectMapper().writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            if(stringValues.size()>1)
                return stringValues.toString();
            return (String) values.get(0);
        }catch (Exception e){
            return null;
        }

    }
    public static CustomUser getCustomUser(Message<?> message){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        AbstractAuthenticationToken userToken = (AbstractAuthenticationToken) accessor.getUser();
        assert userToken != null;
        return (CustomUser) userToken.getPrincipal();
    }
}
