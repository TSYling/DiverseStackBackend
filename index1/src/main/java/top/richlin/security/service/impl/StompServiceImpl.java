package top.richlin.security.service.impl;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import top.richlin.security.dao.CustomUserDao;
import top.richlin.security.entity.*;
import top.richlin.security.service.StompService;
import top.richlin.security.template.ResponseMessageTemplate;
import top.richlin.security.util.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StompServiceImpl
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/31 23:35
 * @description
 */
@Service
public class StompServiceImpl implements StompService {
    private final SimpMessagingTemplate template;
    private final Rooms rooms;
    private final CustomUserDao userDao;

    @Autowired
    public StompServiceImpl(SimpMessagingTemplate template, Rooms rooms,CustomUserDao userDao) {
        this.template = template;
        this.rooms = rooms;
        this.userDao = userDao;
    }

    @Override
    public ResponseMessage createRoom(Message<?> message, String password) {
        CustomUser user = MessageUtils.getCustomUser(message);
        System.out.println(message);
        if(ObjectUtil.isNull(user)){
            return ResponseMessageTemplate.getFailResponseMessage("error","用户未登录",ResponseMessageTemplate.CREATE_ROOM);
        }
        int roomId = 0;
        if(StringUtils.isEmpty(password)||!StringUtils.isAlphanumeric(password)){
            // 判断输入的账号密码是否为数字和字母组成 不是则忽略
            roomId = rooms.createRoom(user);
        }
        else{
            roomId = rooms.createRoomWithPassword(user,password);
        }

        return ResponseMessageTemplate.getSuccessResponseMessage("roomId",roomId,ResponseMessageTemplate.CREATE_ROOM);
    }

    @Override
    public ResponseMessage dissolveRoom(Message<?> message, String roomId) {
        CustomUser user = MessageUtils.getCustomUser(message);
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.DISSOLVE_ROOM);
            // 判断输入的房间号id是否由数字组成
        try{
            rooms.dissolveRoom(user,Integer.parseInt(roomId));
            return ResponseMessageTemplate.getSuccessResponseMessage("data",roomId+"解散成功！",ResponseMessageTemplate.DISSOLVE_ROOM);
        }catch (Exception e){
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.DISSOLVE_ROOM);
        }


    }

    @Override
    public ResponseMessage sendMessageAtRoom(Message<?> message, String roomId) {
        CustomUser user = MessageUtils.getCustomUser(message);
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.SEND);

        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.SEND);
        }
        if(selectedRoom.isMember(user)&&!selectedRoom.isProhibit(user)&&!selectedRoom.isBanned(user)){
            // 发给房间里的所有人
            sendToAllUserInTheRoom(selectedRoom,MessageUtils.getMessage(message));

            return ResponseMessageTemplate.getSuccessResponseMessage("status","发送成功",ResponseMessageTemplate.SEND);
        }else {
            return ResponseMessageTemplate.getFailResponseMessage("error","消息发送失败",ResponseMessageTemplate.SEND);
        }
    }

    @Override
    public ResponseMessage sendSDKAtRoom(Message<?> message, String roomId){
        CustomUser user = MessageUtils.getCustomUser(message);
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.SEND);

        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.SEND);
        }
        if(selectedRoom.isMember(user)&&!selectedRoom.isProhibit(user)&&!selectedRoom.isBanned(user)){
            // 发给房间里的所有人除了自己
            sendToAllUserOfSDKInTheRoomExceptSelf(user.getId(),selectedRoom,MessageUtils.getPayloadString(message));

            return ResponseMessageTemplate.getSuccessResponseMessage("status","发送成功",ResponseMessageTemplate.SEND);
        }else {
            return ResponseMessageTemplate.getFailResponseMessage("error","消息发送失败",ResponseMessageTemplate.SEND);
        }
    }

    @Override
    public ResponseMessage getRoomsInfo(Message<?> message) {
        Map<Integer, Room> roomsInfo = rooms.getRooms();
        ResponseMessage responseMessage = ResponseMessageTemplate.getSuccessResponseMessage();
        roomsInfo.forEach((roomId,room)->{
            Map<String,String> roomSimpleInfo = new HashMap<>();
            roomSimpleInfo.put("roomName",room.getRoomName());
            roomSimpleInfo.put("roomSize",room.getRoomSize().toString());
            roomSimpleInfo.put("usePassword",room.isUsePassword()? "true":"false");
            responseMessage.getContextMap().put(roomId.toString(), roomSimpleInfo);
        });
        responseMessage.setType(ResponseMessageTemplate.ROOMS_INFO);
        return responseMessage;
    }

    @Override
    public ResponseMessage getRoomInfo(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.ROOM_INFO);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.ROOM_INFO);
        }
        RoomDisplayInfo roomInfo = new RoomDisplayInfo(selectedRoom);
        return ResponseMessageTemplate.getSuccessResponseMessage("data",roomInfo,ResponseMessageTemplate.ROOM_INFO);
    }

    @Override
    public ResponseMessage changeRoomName(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.CHANGE_ROOM_NAME);
        String newRoomName = MessageUtils.get(message, "roomName");
        if(newRoomName==null||newRoomName.isEmpty()||newRoomName.isBlank())
            return ResponseMessageTemplate.getFailResponseMessage("error","房间名不能为空",ResponseMessageTemplate.CHANGE_ROOM_NAME);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.CHANGE_ROOM_NAME);
        }
        CustomUser user = MessageUtils.getCustomUser(message);
        if(selectedRoom.isAdmin(user)||selectedRoom.getRoomOwnerId().equals(user.getId())){
            selectedRoom.setRoomName(newRoomName);
            // 通知房间所有人
            sendToAllUserInTheRoom(selectedRoom,"房间名称已修改");
            return ResponseMessageTemplate.getFailResponseMessage("status","房间名修改成功",ResponseMessageTemplate.CHANGE_ROOM_NAME);
        }
        return ResponseMessageTemplate.getFailResponseMessage("error","权限不足",ResponseMessageTemplate.CHANGE_ROOM_NAME);
    }

    @Override
    public ResponseMessage changeRoomPassword(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.CHANGE_ROOM_PASSWORD);
        String newRoomPassword = MessageUtils.get(message, "roomPassword");
        if(!StringUtils.isAlphanumeric(newRoomPassword))
            return ResponseMessageTemplate.getFailResponseMessage("error","新密码格式错误",ResponseMessageTemplate.CHANGE_ROOM_PASSWORD);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.CHANGE_ROOM_PASSWORD);
        }
        CustomUser user = MessageUtils.getCustomUser(message);
        if(selectedRoom.isAdmin(user)||selectedRoom.getRoomOwnerId().equals(user.getId())){
            selectedRoom.setPassword(newRoomPassword);
            selectedRoom.setUsePassword(true);
            // 通知房间所有人
            sendToAllUserInTheRoom(selectedRoom,"房间密码已修改");
            return ResponseMessageTemplate.getFailResponseMessage("status","房间密码修改成功",ResponseMessageTemplate.CHANGE_ROOM_PASSWORD);
        }
        return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.CHANGE_ROOM_PASSWORD);
    }

    @Override
    public ResponseMessage getMembers(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.MEMBERS_INFO);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.MEMBERS_INFO);
        }
        ConcurrentHashMap<Integer, RoomUserInfo> members = selectedRoom.getMembers();
        return ResponseMessageTemplate.getSuccessResponseMessage("data",members,ResponseMessageTemplate.MEMBERS_INFO);
    }

    @Override
    public ResponseMessage getAdmins(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.ADMINS_INFO);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.ADMINS_INFO);
        }
        ConcurrentHashMap<Integer, RoomUserInfo> admins = selectedRoom.getAdminMembers();
        return ResponseMessageTemplate.getSuccessResponseMessage("data",admins,ResponseMessageTemplate.ADMINS_INFO);
    }

    @Override
    public ResponseMessage setAdmin(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.SET_ADMIN);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.SET_ADMIN);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.SET_ADMIN);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.addAdminOnce(userSender,user);
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已添加",ResponseMessageTemplate.SET_ADMIN);
        } catch (RuntimeException e) {
           return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.SET_ADMIN);
        }
    }

    @Override
    public ResponseMessage removeAdmin(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.REMOVE_ADMIN);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.REMOVE_ADMIN);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.REMOVE_ADMIN);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.removeAdminOnce(userSender,user);
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已移除",ResponseMessageTemplate.REMOVE_ADMIN);
        } catch (RuntimeException e) {
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.REMOVE_ADMIN);
        }
    }

    @Override
    public ResponseMessage getBanners(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.BANNERS_INFO);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.BANNERS_INFO);
        }
        ConcurrentHashMap<Integer, RoomUserInfo> banners = selectedRoom.getBanedMembers();
        return ResponseMessageTemplate.getSuccessResponseMessage("data",banners,ResponseMessageTemplate.BANNERS_INFO);
    }

    @Override
    public ResponseMessage setBanner(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.SET_BANNER);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.SET_BANNER);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.SET_BANNER);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.addBannerOnce(userSender,user);
            // 踢出
            rooms.leave(new RoomUserInfo(user),selectedRoom.getId());
            // 告诉对方自己被禁
            template.convertAndSendToUser(
                    user.getUsername(),
                    "/topic/status",
                    ResponseMessageTemplate.getSuccessResponseMessage("warning","您已被禁止加入房间"+roomId,ResponseMessageTemplate.WARNING));
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已添加",ResponseMessageTemplate.SET_BANNER);
        } catch (RuntimeException e) {
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.SET_BANNER);
        }
    }

    @Override
    public ResponseMessage removeBanner(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.REMOVE_BANNER);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.REMOVE_BANNER);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.REMOVE_BANNER);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.removeBannerOnce(userSender,user);
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已移除",ResponseMessageTemplate.REMOVE_BANNER);
        } catch (RuntimeException e) {
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.REMOVE_BANNER);
        }
    }

    @Override
    public ResponseMessage getProhibits(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.PROHIBITS_INFO);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.PROHIBITS_INFO);
        }
        ConcurrentHashMap<Integer, RoomUserInfo> prohibits = selectedRoom.getProhibitMembers();
        return ResponseMessageTemplate.getSuccessResponseMessage("data",prohibits,ResponseMessageTemplate.PROHIBITS_INFO);
    }

    @Override
    public ResponseMessage setProhibit(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.SET_PROHIBIT);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.SET_PROHIBIT);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.SET_PROHIBIT);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.addProhibitOnce(userSender,user);
            // 告诉对方自己被禁言
            template.convertAndSendToUser(
                    user.getUsername(),
                    "/topic/status",
                    ResponseMessageTemplate.getSuccessResponseMessage("warning","您已被禁止发言"+roomId,ResponseMessageTemplate.WARNING));
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已添加",ResponseMessageTemplate.SET_PROHIBIT);
        } catch (RuntimeException e) {
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.SET_PROHIBIT);
        }
    }

    @Override
    public ResponseMessage removeProhibit(Message<?> message, String roomId) {
        if(StringUtils.isEmpty(roomId)||!StringUtils.isNumeric(roomId))
            return ResponseMessageTemplate.getFailResponseMessage("error","roomId传参错误",ResponseMessageTemplate.REMOVE_PROHIBIT);
        String userId = MessageUtils.get(message, "userId");
        CustomUser user = userDao.selectById(userId);
        if(ObjectUtil.isNull(user))
            return ResponseMessageTemplate.getFailResponseMessage("error","用户不存在",ResponseMessageTemplate.REMOVE_PROHIBIT);
        Room selectedRoom = rooms.getSelectedRoom(Integer.parseInt(roomId));
        if(ObjectUtil.isNull(selectedRoom)){
            return ResponseMessageTemplate.getFailResponseMessage("error","room不存在",ResponseMessageTemplate.REMOVE_PROHIBIT);
        }
        CustomUser userSender = MessageUtils.getCustomUser(message);
        try {
            selectedRoom.removeProhibitOnce(userSender,user);
            return ResponseMessageTemplate.getSuccessResponseMessage("error","已移除",ResponseMessageTemplate.REMOVE_PROHIBIT);
        } catch (RuntimeException e) {
            return ResponseMessageTemplate.getFailResponseMessage("error",e.getMessage(),ResponseMessageTemplate.REMOVE_PROHIBIT);
        }
    }

    private void sendToAllUserInTheRoom(Room room, String payload){
        template.convertAndSend("/room/"+room.getId(),payload);
    }
    private void sendToAllUserOfSDKInTheRoomExceptSelf(Integer senderId,Room room, String payload){
        ConcurrentHashMap<Integer, RoomUserInfo> members = room.getMembers();
        members.forEach((uid,user)->{
            if(!senderId.equals(uid))
                template.convertAndSendToUser(user.getEmail(),"/room/SDK/"+room.getId(),payload);
        });
    }

}
    //广播推送消息
//    @Scheduled(fixedRate = 10000)
//    public void sendTopicMessage() {
//        System.out.println("后台广播推送！");
//        this.template.convertAndSend("/topic/test","123123");
//    }