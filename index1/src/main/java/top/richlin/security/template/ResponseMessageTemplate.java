package top.richlin.security.template;

import cn.hutool.json.JSONUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import top.richlin.security.entity.ResponseMessage;

import java.util.HashMap;

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
    public static ResponseMessage getSuccessResponseMessage(){
        return new ResponseMessage(SUCCESS,new HashMap<>());
    }
    public static ResponseMessage getSuccessResponseMessage(String key,Object value){
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put(key,value);
        return new ResponseMessage(SUCCESS, contextMap);
    }
    public static ResponseMessage getFailResponseMessage(){
        return new ResponseMessage(FAIL,new HashMap<>());
    }
    public static ResponseMessage getFailResponseMessage(String key,Object value){
        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put(key,value);
        return new ResponseMessage(FAIL, contextMap);
    }

    public static void sendSubscribeFail(SimpMessagingTemplate template, StompHeaderAccessor accessor,String errorDescribe){
        String errorDescribeToUse = "订阅已被拒绝";
        if(!errorDescribe.equals("")){
            errorDescribeToUse = errorDescribe;
        }
        template.convertAndSendToUser(
                accessor.getUser().getName(),
                "/topic/status",
                getFailResponseMessage("error", errorDescribeToUse));
    }
    public static void sendSubscribeSuccess(SimpMessagingTemplate template, StompHeaderAccessor accessor,String successDescribe){
        String successDescribeToUse = "订阅成功";
        if(!successDescribe.equals("")){
            successDescribeToUse = successDescribe;
        }
        template.convertAndSendToUser(
                accessor.getUser().getName(),
                "/topic/status",
                getSuccessResponseMessage("status", successDescribeToUse));
    }

}
