package top.richlin.security.entity;

import lombok.Data;

/**
 * RoomUserInfo
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/12 15:11
 * @description
 */
@Data
public class RoomUserInfo{
    private int id;
    private String email;
    private String name;
    private boolean isSuperAdmin;
    public RoomUserInfo(CustomUser user){
        id = user.getId();
        email = user.getUsername();
        name = user.getName();
        isSuperAdmin = user.isAdmin();
    }
}
