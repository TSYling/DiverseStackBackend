package top.richlin.security.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

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
    private static Random random = new Random();
    public static Map<Integer,Room> rooms = new ConcurrentHashMap<>();
    public static int createRoom(CustomUser user){
        int roomId;
        do{
            roomId = RandomNum();
        }while(rooms.putIfAbsent(roomId,new Room(roomId,user))!=null);
        return roomId;
    }
    public static int createRoomWithPassword(CustomUser user,String password){
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
    public static void dissolveRoom(int roomId,CustomUser user) throws RuntimeException{
        Room room = rooms.get(roomId);
        if(room.getRoomOwnerId().intValue() == user.getId()||user.isAdmin()){
            rooms.remove(roomId);
        }else {
            throw new RuntimeException("只有房主或者超级管理员才有能解散房间");
        }
    }
    public static int getRoomNum(){
        return rooms.size();
    }
    public Room getSelectedRoom(int id){
        return rooms.get(id);
    }

    private static int RandomNum() {
        return random.nextInt(10000,999999);
    }
}
