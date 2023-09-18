package top.richlin.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RestController;
import top.richlin.security.entity.ResponseMessage;
import top.richlin.security.service.StompService;

/**
 * StompController
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/30 19:29
 */
@RestController
@MessageMapping("/room")
public class StompController {
    private final StompService service;

    @Autowired
    public StompController(StompService service) {
        this.service = service;
    }


    @MessageMapping("/createRoom/{password}")
    @SendToUser("/topic/status")
    /*
      !important 注意注意  这边 开头的/Topic 必须要为设置中已经设置过的   否者subscribe无效
     */
    public ResponseMessage createRoom(Message<?> message,@DestinationVariable String password){
        return service.createRoom(message,password);
    }
    @MessageMapping("/dissolveRoom/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage dissolveRoom(Message<?> message,@DestinationVariable String roomId){
        return service.dissolveRoom(message,roomId);
    }
    @MessageMapping("/send/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage sendMessageAtRoom(Message<?> message,@DestinationVariable String roomId){
        return service.sendMessageAtRoom(message,roomId);
    }
    @MessageMapping("/sendSDK/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage sendSDKAtRoom(Message<?> message,@DestinationVariable String roomId){
        return service.sendSDKAtRoom(message,roomId);
    }

    /**
     *
     * @param message
     * @return 返回指定范围内房间信息
     */
    @MessageMapping("/roomsInfo")
    @SendToUser("/topic/status")
    public ResponseMessage roomsInfo(Message<?> message){
        return service.getRoomsInfo(message);
    }

    /**
     *
     * @param message
     * @param roomId
     * @return 返回指定房间的详细信息
     */
    @MessageMapping("/roomInfo/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage roomInfo(Message<?> message,@DestinationVariable String roomId){
        return service.getRoomInfo(message,roomId);
    }

    /**
     * 修改房间名
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/changeName/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage changeName(Message<?> message,@DestinationVariable String roomId){
        return service.changeRoomName(message,roomId);
    }
    /**
     * 修改房间密码
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/changePassword/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage changePassword(Message<?> message,@DestinationVariable String roomId){
        return service.changeRoomPassword(message,roomId);
    }

    /**
     * 获取指定房间的成员
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/members/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage getMembers(Message<?> message,@DestinationVariable String roomId){
        return service.getMembers(message,roomId);
    }

    /**
     * 返回指定房间的管理员
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/admins/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage getAdmins(Message<?> message,@DestinationVariable String roomId){
        return service.getAdmins(message,roomId);
    }

    /**
     * 设置房间的管理员
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/setAdmin/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage setAdmin(Message<?> message,@DestinationVariable String roomId){
        return service.setAdmin(message,roomId);
    }
    /**
     * 移除房间的管理员
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/removeAdmin/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage removeAdmin(Message<?> message,@DestinationVariable String roomId){
        return service.removeAdmin(message,roomId);
    }

    /**
     * 获取房间黑名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/banners/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage getBanners(Message<?> message,@DestinationVariable String roomId){
        return service.getBanners(message,roomId);
    }

    /**
     * 设置房间黑名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/setBanner/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage setBanner(Message<?> message,@DestinationVariable String roomId){
        return service.setBanner(message,roomId);
    }

    /**
     * 移除房间黑名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/removeBanner/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage removeBanner(Message<?> message,@DestinationVariable String roomId){
        return service.removeBanner(message,roomId);
    }

    /**
     * 获取房间禁言名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/prohibits/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage getProhibits(Message<?> message,@DestinationVariable String roomId){
        return service.getProhibits(message,roomId);
    }

    /**
     * 设置房间禁言名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/setProhibit/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage setProhibit(Message<?> message,@DestinationVariable String roomId){
        return service.setProhibit(message,roomId);
    }

    /**
     * 移除房间禁言名单
     * @param message
     * @param roomId
     * @return
     */
    @MessageMapping("/removeProhibit/{roomId}")
    @SendToUser("/topic/status")
    public ResponseMessage removeProhibit(Message<?> message,@DestinationVariable String roomId){
        return service.removeProhibit(message,roomId);
    }
}
