package top.richlin.security.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import top.richlin.security.dao.CustomUserDao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Room
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/2 15:05
 * @description
 */
@Data
public class Room{
    /**
     * 房间号
     */
    private Integer id;
    /**
     * 房主Id
     */
    private Integer roomOwnerId;
    /**
     * 房间大小 0代表无限制
     */
    private Integer roomSize = 0;
    /**
     * 房间成员信息 userId UserInfo
     */
    private ConcurrentHashMap<Integer,RoomUserInfo> members = new ConcurrentHashMap<>();
    /**
     * 设置管理员列表
     */
    private ConcurrentHashMap<Integer,RoomUserInfo> adminMembers = new ConcurrentHashMap<>();
    /**
     * 封禁成员信息 禁言
     */
    private ConcurrentHashMap<Integer,RoomUserInfo> prohibitMembers = new ConcurrentHashMap<>();
    /**
     * 黑名单成员
     * 无法加入房间
     */
    private ConcurrentHashMap<Integer,RoomUserInfo> banedMembers = new ConcurrentHashMap<>();
    /**
     * 房间是否需要密码
     */
    private boolean usePassword = false;
    /**
     * 房间密码
     */
    private String password = null;
    private CustomUserDao userDao;

    public Room(Integer id,CustomUser user){
        this.id = id;
        members.put(user.getId(),new RoomUserInfo(user));
        roomOwnerId = user.getId();
    }

    /**
     * 用户加入房间
     * 用户不在封禁列表 才能加入房间
     * @param user
     * @return 加入房间是否成功
     */
    public void join(CustomUser user,String password) throws RuntimeException{
        Integer uid = user.getId();
        if(!isBanned(user)){
            if(this.usePassword){
                // 需要密码 超级管理员直接进入
                if(user.isAdmin()||this.password.equals(password)){
                    members.putIfAbsent(uid,new RoomUserInfo(user));
                    return;
                }
                else{
                    throw new RuntimeException("密码错误");
                }
            }
            members.putIfAbsent(uid, new RoomUserInfo(user));
        }
        else
            throw new RuntimeException("用户被禁止加入房间");
    }

    /**
     * 离开房间
     * @param user
     */
    public void leave(CustomUser user){
        Integer uid = user.getId();
        members.remove(uid);
    }

    /**
     *
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     * 设置一次性管理员
     * 房间关闭后失效
     */
    public void addAdminOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()){
            adminMembers.putIfAbsent(selectedUser.getId(),new RoomUserInfo(selectedUser));
        }else {
            throw new RuntimeException("权限不足");
        }
    }

    /**
     *
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     * 移除一次性管理员
     */
    public void removeAdminOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()){
            if(ObjectUtil.isNull(adminMembers.remove(selectedUser.getId()))){
                throw new RuntimeException("该用户不存在");
            }
        }else {
            throw new RuntimeException("权限不足");
        }
    }

    /**
     * 禁言用户
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     */
    public void addProhibitOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        // superAdmin无法被禁言
        if(selectedUser.isAdmin()){
            throw new RuntimeException("无法禁言");
        }
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()||isAdmin(user)){
            prohibitMembers.putIfAbsent(selectedUser.getId(),new RoomUserInfo(selectedUser));
        }else {
            throw new RuntimeException("权限不足");
        }
    }

    /**
     * 移除用户禁言
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     */
    public void removeProhibitOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()||isAdmin(user)){
            if(ObjectUtil.isNull(prohibitMembers.remove(selectedUser.getId()))){
                throw new RuntimeException("该用户不存在");
            }
        }else {
            throw new RuntimeException("权限不足");
        }
    }

    /**
     * 封禁用户
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     */
    public void addBannerOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        // 用户若是是管理员 就不能封禁
        if(isAdmin(selectedUser)){
            if(selectedUser.isAdmin()){
                throw new RuntimeException("无法封禁");
            }
            throw new RuntimeException("用户为管理员，请先移除管理员后再进行");
        }
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()||isAdmin(user)){
            banedMembers.putIfAbsent(selectedUser.getId(),new RoomUserInfo(selectedUser));
        }else {
            throw new RuntimeException("权限不足");
        }
    }

    /**
     * 移除封禁用户
     * @param user
     * @param selectedUser
     * @throws RuntimeException
     */
    public void removeBannerOnce(CustomUser user,CustomUser selectedUser) throws RuntimeException{
        if(getRoomOwnerId().intValue() == user.getId()||user.isAdmin()||isAdmin(user)){
            if(ObjectUtil.isNull(banedMembers.remove(selectedUser.getId()))){
                throw new RuntimeException("该用户未被封禁");
            }
        }else {
            throw new RuntimeException("权限不足");
        }
    }
    public boolean isAdmin(CustomUser user){
        return !ObjectUtil.isNull(adminMembers.get(user.getId()));
    }
    public boolean isBanned(CustomUser user){
        return !ObjectUtil.isNull(banedMembers.get(user.getId()));
    }
    public boolean isProhibit(CustomUser user){
        return !ObjectUtil.isNull(prohibitMembers.get(user.getId()));
    }

}
@Data
class RoomUserInfo{
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
