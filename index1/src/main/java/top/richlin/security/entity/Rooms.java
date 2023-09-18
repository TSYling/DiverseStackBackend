package top.richlin.security.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.stereotype.Component;
import top.richlin.security.template.ResponseMessageTemplate;
import top.richlin.security.util.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Room
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/2 13:30
 * @description
 */
//@Component
@Data
@Component
public class Rooms {

    private StompConnectPool stompConnectPool;
    private SimpMessagingTemplate messagingTemplate;
    private Random random = new Random();
    private final AbstractSubscribableChannel subscribableChannel; // 可以用来取消订阅
    public Map<Integer,Room> rooms = new ConcurrentHashMap<>();
    @Autowired
    public Rooms(StompConnectPool stompConnectPool, SimpMessagingTemplate messagingTemplate, @Qualifier("brokerChannel") AbstractSubscribableChannel subscribableChannel) {
        this.stompConnectPool = stompConnectPool;
        this.messagingTemplate = messagingTemplate;
        this.subscribableChannel = subscribableChannel;
    }
    public int createRoom(CustomUser user){
        int roomId;
        do{
            roomId = RandomNum();
        }while(rooms.putIfAbsent(roomId,new Room(roomId,user))!=null);
        stompConnectPool.recordCreateRoom(user.getId(),roomId);
        return roomId;
    }
    public int createRoomWithPassword(CustomUser user,String password){
        int roomId;
        Room room = null;
        do{
            roomId = RandomNum();
            room = new Room(roomId, user);
            room.setUsePassword(true);
            room.setPassword(password);
        }while(rooms.putIfAbsent(roomId, room)!=null);
        stompConnectPool.recordCreateRoom(user.getId(),roomId);
        return roomId;
    }
    public void dissolveRoom(CustomUser user,int roomId) throws RuntimeException{
        Room room = rooms.get(roomId);
        if(ObjectUtil.isNull(room)){
            throw new RuntimeException("房间不存在");
        }
        if(room.getRoomOwnerId().intValue() == user.getId()||user.isAdmin()){
            ConcurrentHashMap<Integer, RoomUserInfo> members = room.getMembers();
            rooms.remove(roomId);
            stompConnectPool.recordDissolveRoom(user.getId(),roomId);
            // 用户强制离开房间 采用新线程执行
            Thread thread = new Thread(()->{
                members.forEach((uid,userInfo)->{
                    stompConnectPool.recordLeaveRoom(uid,roomId);
                    this.leave(userInfo,roomId);
                    // 提醒用户已离开房间xxx
                    messagingTemplate.convertAndSendToUser(
                            userInfo.getEmail(),
                            "/topic/status",
                            ResponseMessageTemplate.getSuccessResponseMessage("warning",room.getRoomName()+"("+room.getId()+") 已解散",ResponseMessageTemplate.WARNING)
                    );
                });
            });
            thread.start();
        }else {
            throw new RuntimeException("只有房主或者超级管理员才有能解散房间");
        }
    }
    public int getRoomNum(){
        return rooms.size();
    }
    public Room getSelectedRoom(int id){
        return rooms.get(id);
    }
    public void join(CustomUser user,String password,Integer roomId) throws RuntimeException{
        try{
            Room selectedRoom = getSelectedRoom(roomId);
            boolean isJoin = selectedRoom.join(user, password);
            if (isJoin)
                ResponseMessageTemplate.sendToAllUser(
                        messagingTemplate,
                        "/room/"+roomId,
                        "data",new RoomUserInfo(user).toString(),
                        ResponseMessageTemplate.JOIN);
            stompConnectPool.recordJoinRoom(user.getId(), roomId);
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * 使用户离开房间
     * @param user
     */
    public void leave(RoomUserInfo user,Integer roomId){
        Room selectedRoom = getSelectedRoom(roomId);
        Integer uid = user.getId();
        boolean leaveStatus = false;
        if(!ObjectUtil.isNull(selectedRoom))
            leaveStatus = selectedRoom.leave(uid);
        // 移除subscriber列表
        unSubscribe(uid,roomId);
        if(leaveStatus)
            ResponseMessageTemplate.sendToAllUser(
                    messagingTemplate,
                    "/room/"+roomId,
                    "data",user.toString(),
                    ResponseMessageTemplate.LEAVE);
        // 移除记录
        stompConnectPool.recordLeaveRoom(uid, roomId);


    }

    private int RandomNum() {
        return random.nextInt(10000,999999);
    }
    private void unSubscribe(Integer userId, Integer roomId){
        List<MessageHandler> list =new ArrayList<>(subscribableChannel.getSubscribers());
        MessageHandler messageHandler = list.get(0);
        if(messageHandler instanceof SimpleBrokerMessageHandler){
            /*
            * 配置一个头包含用户的sessionId
            * */
            String sessionId = stompConnectPool.onlineClientSessionIdMap.get(userId);
            String destination = "/room/" + roomId;
            Map<String, String> userSubInfo = stompConnectPool.onlineClientSubscribeInfo.get(userId);
            List<String> subscribeIds = getSubscribeIds(userSubInfo,destination);
            // 从记录中移除 所有订阅
            if(ObjectUtil.isNull(subscribeIds)||subscribeIds.isEmpty())
                return;
            subscribeIds.forEach((subscribeId)->{
                userSubInfo.remove(subscribeId);
                Message<String> message = MessageUtils.createMessage(messagingTemplate, SimpMessageType.UNSUBSCRIBE, sessionId,subscribeId);
                ((SimpleBrokerMessageHandler) messageHandler).getSubscriptionRegistry().unregisterSubscription(message);
            });
        }
    }
    private List<String> getSubscribeIds(Map<String,String> subInfo,String destination){
        if(ObjectUtil.isNull(subInfo)||subInfo.isEmpty())
            return null;
        ArrayList<String> subIds = new ArrayList<>();
        subInfo.forEach((key,value)->{
            if(value.equals(destination))
                subIds.add(key);
        });
        return subIds;
    }
}
//    public final void unregisterSubscription(Message<?> message) {
//        MessageHeaders headers = message.getHeaders();
//
//        SimpMessageType messageType = SimpMessageHeaderAccessor.getMessageType(headers);
//        if (!SimpMessageType.UNSUBSCRIBE.equals(messageType)) {
//            throw new IllegalArgumentException("Expected UNSUBSCRIBE: " + message);
//        }
//
//        String sessionId = SimpMessageHeaderAccessor.getSessionId(headers);
//        if (sessionId == null) {
//            if (logger.isErrorEnabled()) {
//                logger.error("No sessionId in " + message);
//            }
//            return;
//        }
//
//        String subscriptionId = SimpMessageHeaderAccessor.getSubscriptionId(headers);
//        if (subscriptionId == null) {
//            if (logger.isErrorEnabled()) {
//                logger.error("No subscriptionId " + message);
//            }
//            return;
//        }
//
//        removeSubscriptionInternal(sessionId, subscriptionId, message);
//    }