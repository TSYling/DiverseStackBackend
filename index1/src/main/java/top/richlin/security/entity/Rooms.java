package top.richlin.security.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import top.richlin.security.template.ResponseMessageTemplate;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    public Map<Integer,Room> rooms = new ConcurrentHashMap<>();
    @Autowired
    public Rooms(StompConnectPool stompConnectPool,SimpMessagingTemplate messagingTemplate) {
        this.stompConnectPool = stompConnectPool;
        this.messagingTemplate = messagingTemplate;
    }
    public int createRoom(CustomUser user){
        int roomId;
        do{
            roomId = RandomNum();
        }while(rooms.putIfAbsent(roomId,new Room(roomId,user))!=null);
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
            // 用户强制离开房间 采用新线程执行
            Thread thread = new Thread(()->{
                members.forEach((uid,userInfo)->{
                    stompConnectPool.recordLeaveRoom(uid,room.getId());
                    // 提醒用户已离开房间xxx
                    messagingTemplate.convertAndSendToUser(
                            userInfo.getEmail(),
                            "/topic/status",
                            ResponseMessageTemplate.getSuccessResponseMessage("warning","您已离开房间："+room.getId())
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
            selectedRoom.join(user,password);
            stompConnectPool.recordJoinRoom(user.getId(), roomId);
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * 离开房间
     * @param user
     */
    public void leave(CustomUser user,Integer roomId){
        Room selectedRoom = getSelectedRoom(roomId);
        selectedRoom.leave(user);
        // 移除记录
        stompConnectPool.recordLeaveRoom(user.getId(), roomId);

    }

    private int RandomNum() {
        return random.nextInt(10000,999999);
    }
}
