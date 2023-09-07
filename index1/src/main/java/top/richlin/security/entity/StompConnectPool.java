package top.richlin.security.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
     * 存放用户加入了的房间 用户ID-房间ID
     */
    public Map<Integer, ArrayList<Integer>> onlineClientRoomMap = new ConcurrentHashMap<>();
    public void recordOnline(CustomUser user){
        onlineClientMap.putIfAbsent(user.getId(),user);
    }
    public void recordOffline(CustomUser user){
        onlineClientMap.remove(user.getId());
    }
    public void recordJoinRoom(Integer userId,Integer roomId){
        ArrayList<Integer> roomIds = onlineClientRoomMap.get(userId);
        if(ObjectUtil.isNull(roomIds)||roomIds.size()==0){
            ArrayList<Integer> newRoomIds = new ArrayList<>();
            newRoomIds.add(roomId);
            onlineClientRoomMap.put(userId,newRoomIds);
            return;
        }
        roomIds.add(roomId);
    }
    public void recordLeaveRoom(Integer userId,Integer roomId){
        ArrayList<Integer> roomIds = onlineClientRoomMap.get(userId);
        if(ObjectUtil.isNull(roomIds)||roomIds.size()==0){
            onlineClientRoomMap.remove(userId);
            return;
        }
        roomIds.remove(roomId);
        if(roomIds.size()==0){
            onlineClientRoomMap.remove(userId);
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
                rooms.leave(user,roomId);
            });
    }

}
