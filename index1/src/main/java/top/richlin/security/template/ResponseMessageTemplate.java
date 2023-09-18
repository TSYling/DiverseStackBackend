package top.richlin.security.template;

import cn.hutool.json.JSONUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import top.richlin.security.entity.ResponseMessage;

import java.util.HashMap;
import java.util.Objects;

/**
 * ResponseMessageTemplate
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/2 20:26
 * @description
 */
public class ResponseMessageTemplate {
    public final static int SUCCESS = 200;
    public final static int FAIL = -1;
    public final static String CREATE_ROOM = "createRoom";
    public final static String DISSOLVE_ROOM = "dissolveRoom";
    public final static String SUBSCRIBE = "subscribe";
    public final static String WARNING = "warning";
    public final static String SEND = "send";
    public final static String ROOMS_INFO = "roomsInfo";
    public final static String ROOM_INFO = "roomInfo";
    public final static String MEMBERS_INFO = "membersInfo";
    public final static String ADMINS_INFO = "adminsInfo";
    public final static String BANNERS_INFO = "bannersInfo";
    public final static String PROHIBITS_INFO = "prohibitsInfo";
    public static final String CHANGE_ROOM_NAME = "changeRoomName";
    public static final String CHANGE_ROOM_PASSWORD = "changeRoomPassword";
    public static final String SET_ADMIN = "setAdmin";
    public static final String SET_BANNER = "setBanner";
    public static final String SET_PROHIBIT = "setProhibit";
    public static final String REMOVE_ADMIN = "removeAdmin";
    public static final String REMOVE_BANNER = "removeBanner";
    public static final String REMOVE_PROHIBIT = "removeProhibit";
    public static final String JOIN = "join";
    public static final String LEAVE = "leave";

    public static ResponseMessage getSuccessResponseMessage(){
        return new ResponseMessage(SUCCESS,"",new HashMap<>());
    }
    public static ResponseMessage getSuccessResponseMessage(String key,Object value,String type){
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put(key,value);
        return new ResponseMessage(SUCCESS, type,contextMap);
    }
    public static ResponseMessage getFailResponseMessage(){
        return new ResponseMessage(FAIL,"",new HashMap<>());
    }
    public static ResponseMessage getFailResponseMessage(String key,Object value,String type){
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put(key,value);
        return new ResponseMessage(FAIL,type, contextMap);
    }

    public static void sendSubscribeFail(SimpMessagingTemplate template, StompHeaderAccessor accessor,String errorDescribe){
        String errorDescribeToUse = "订阅已被拒绝";
        if(!errorDescribe.equals("")){
            errorDescribeToUse = errorDescribe;
        }
        template.convertAndSendToUser(
                Objects.requireNonNull(accessor.getUser()).getName(),
                "/topic/status",
                getFailResponseMessage("error", errorDescribeToUse,SUBSCRIBE));
    }
    public static void sendSubscribeSuccess(SimpMessagingTemplate template, StompHeaderAccessor accessor,String successDescribe){
        String successDescribeToUse = "订阅成功";
        if(!successDescribe.equals("")){
            successDescribeToUse = successDescribe;
        }
        template.convertAndSendToUser(
                Objects.requireNonNull(accessor.getUser()).getName(),
                "/topic/status",
                getSuccessResponseMessage("status",successDescribeToUse,SUBSCRIBE));
    }
    public static void sendToAllUser(SimpMessagingTemplate template,String destination,String key,String value,String type){
        template.convertAndSend(destination,getSuccessResponseMessage(key,value,type));
    }

}
