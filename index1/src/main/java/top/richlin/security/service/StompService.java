package top.richlin.security.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import top.richlin.security.entity.ResponseMessage;

/**
 * StompService
 *
 * @author wsl
 * @version 1.0
 * @date 2023/8/31 23:30
 * @description
 */
public interface StompService {
    ResponseMessage createRoom(Message<?> message,String password);
    ResponseMessage dissolveRoom(Message<?> message,String roomId);
    ResponseMessage sendMessageAtRoom(Message<?> message,String roomId);
    ResponseMessage sendSDKAtRoom(Message<?> message, String roomId);
    ResponseMessage getRoomsInfo(Message<?> message);

    ResponseMessage getRoomInfo(Message<?> message, String roomId);

    ResponseMessage changeRoomName(Message<?> message, String roomId);

    ResponseMessage changeRoomPassword(Message<?> message, String roomId);

    ResponseMessage getMembers(Message<?> message, String roomId);

    ResponseMessage getAdmins(Message<?> message, String roomId);

    ResponseMessage setAdmin(Message<?> message, String roomId);

    ResponseMessage removeAdmin(Message<?> message, String roomId);

    ResponseMessage getBanners(Message<?> message, String roomId);

    ResponseMessage setBanner(Message<?> message, String roomId);

    ResponseMessage removeBanner(Message<?> message, String roomId);

    ResponseMessage getProhibits(Message<?> message, String roomId);

    ResponseMessage setProhibit(Message<?> message, String roomId);

    ResponseMessage removeProhibit(Message<?> message, String roomId);


}
