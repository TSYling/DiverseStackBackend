package top.richlin.security.entity;

import lombok.Data;

/**
 * RoomDisplayInfo
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/13 15:20
 * @description
 */
@Data
public class RoomDisplayInfo {
    private String roomId;
    private String roomOwner;
    private String roomName;
    private String roomSize;
    private String membersNum;
    public RoomDisplayInfo(Room room){
        roomId = room.getId().toString();
        roomOwner = room.getMembers().get(room.getRoomOwnerId()).getName();
        roomName = room.getRoomName();
        roomSize = room.getRoomSize().toString();
        membersNum = Integer.toString(room.getMembers().size());
    }
}
