package top.richlin.security.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import top.richlin.security.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StompConnectPool
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/1 23:38
 * 每个建立了stomp连接的用户都要在这保存相关数据
 */
@Component
@Data
public class StompConnectPool {
    private Rooms rooms;

    @Autowired
    @Lazy
    public StompConnectPool(Rooms rooms) {
        this.rooms = rooms;
    }

    /**
     * 存放所有在线的客户端 用户ID 用户信息
     */
    public Map<Integer, CustomUser> onlineClientMap = new ConcurrentHashMap<>();
    /**
     * 存放所有在线的客户端 用户ID 用户sessionId
     */
    public Map<Integer, String> onlineClientSessionIdMap = new ConcurrentHashMap<>();
    /**
     * 存放用户订阅信息 方便管理 用户Id  一个地址可以有多个id
     * <subscribeId,subscribeDestination>
     */
    public Map<Integer,Map<String,String>> onlineClientSubscribeInfo = new ConcurrentHashMap<>();
    /**
     * 存放用户加入了的房间 用户ID-房间ID
     */
    public Map<Integer, ArrayList<Integer>> onlineClientRoomMap = new ConcurrentHashMap<>();

    /**
     * 存放用户创建的房间
     */
    public Map<Integer, ArrayList<Integer>> onlineClientRoomOwnerMap = new ConcurrentHashMap<>();
    public void recordOnline(CustomUser user, Message<?> message){
        onlineClientMap.putIfAbsent(user.getId(),user);
        String sessionId = MessageUtils.getSessionId(message);
        onlineClientSessionIdMap.putIfAbsent(user.getId(), sessionId);
    }
    public void recordOffline(CustomUser user, Message<?> message){
        onlineClientMap.remove(user.getId());
        String sessionId = MessageUtils.getSessionId(message);
        onlineClientSessionIdMap.putIfAbsent(user.getId(), sessionId);
    }
    public void recordJoinRoom(Integer userId,Integer roomId){
        mapAddItem(userId, roomId, onlineClientRoomMap);
    }
    public void recordLeaveRoom(Integer userId,Integer roomId){
        mapRemoveItem(userId, roomId, onlineClientRoomMap);
    }
    public void recordCreateRoom(Integer userId,Integer roomId){
        mapAddItem(userId, roomId, onlineClientRoomOwnerMap);
    }
    public void recordDissolveRoom(Integer userId,Integer roomId){
        mapRemoveItem(userId, roomId, onlineClientRoomOwnerMap);
    }

    private void mapAddItem(Integer userId, Integer roomId, Map<Integer, ArrayList<Integer>> map) {
        ArrayList<Integer> roomIds = map.get(userId);
        if(ObjectUtil.isNull(roomIds)||roomIds.size()==0){
            ArrayList<Integer> newRoomIds = new ArrayList<>();
            newRoomIds.add(roomId);
            map.putIfAbsent(userId,newRoomIds);
            return;
        }
        roomIds.add(roomId);
    }



    private void mapRemoveItem(Integer userId, Integer roomId, Map<Integer, ArrayList<Integer>> map) {
        ArrayList<Integer> roomIds = map.get(userId);
        if(ObjectUtil.isNull(roomIds)||roomIds.size()==0){
            map.remove(userId);
            return;
        }
        roomIds.remove(roomId);
        if(roomIds.size()==0){
            map.remove(userId);
        }
    }

    /**
     * 用户加入过的房间全部退出
     * @param user
     */
    public void leaveAllRoom(CustomUser user){
        /**
         * 清除本地记录以及room记录
         */
        Integer uid = user.getId();
        ArrayList<Integer> roomIds = onlineClientRoomMap.get(uid);
        onlineClientRoomMap.remove(uid);
        if(!ObjectUtil.isNull(roomIds)&&roomIds.size()!=0)
            roomIds.forEach((roomId)->{
                rooms.leave(new RoomUserInfo(user),roomId);
            });
    }
    public void dissolveAllRoom(CustomUser user){
        Integer uid = user.getId();
        ArrayList<Integer> roomIds = onlineClientRoomOwnerMap.get(uid);
        onlineClientRoomOwnerMap.remove(uid);
        if(!ObjectUtil.isNull(roomIds)&&roomIds.size()!=0)
            roomIds.forEach((roomId)->{
                rooms.dissolveRoom(user,roomId);
            });
    }

}
