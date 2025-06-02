package project.vmo.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.util.MessageCreator;
import project.vmo.domain.UserSession;
import project.vmo.session.UserSessionFactory;
import project.vmo.dto.CreateRoomDto;
import project.vmo.dto.JoinRoomDto;
import project.vmo.domain.Room;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RoomService {
    private final Logger log = LoggerFactory.getLogger(RoomService.class);
    private final KurentoClient kurentoClient;
    private final UserSessionFactory userSessionFactory;

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    public RoomService(KurentoClient kurentoClient, UserSessionFactory userSessionFactory) {
        this.kurentoClient = kurentoClient;
        this.userSessionFactory = userSessionFactory;
    }

    public UserSession createRoom(WebSocketSession session, CreateRoomDto dto) {
        Room room = new Room(kurentoClient.createMediaPipeline(), session.getId(), dto.username());
        rooms.put(room.getRoomId(), room);

        log.info("사용자 {} / {} 가 방 {} 을 생성했습니다.", dto.username(), session.getId(), room.getRoomId());

        UserSession newUser = userSessionFactory.create(dto.username(), room.getRoomId(), dto.audioOn(), dto.videoOn(), session, room.getPipeline());
        notifyNewUserJoined(newUser);
        room.addParticipant(newUser);
        room.changeRoomLeader(session.getId(), dto.username());

        JsonObject response = MessageCreator.createRoomCreatedMessage(dto, newUser, room);
        SendService.sendMessage(newUser.getSession(), response);

        return newUser;
    }

    public UserSession joinRoom(WebSocketSession session, JoinRoomDto dto) {
        Room room = getRoomById(dto.roomId());

        UserSession newUser = userSessionFactory.create(dto.username(), room.getRoomId(), dto.audioOn(), dto.videoOn(), session, room.getPipeline());
        notifyNewUserJoined(newUser);
        room.addParticipant(newUser);

        sendExistingParticipantsList(newUser);
        return newUser;
    }

    private void notifyNewUserJoined(UserSession newUser) {
        Room room = getRoomById(newUser.getRoomId());
        JsonObject message = MessageCreator.createNewUserJoinedMessage(newUser);

        log.debug("방 {}: 새로운 참가자 {} / {} 입장 알림 전송", newUser.getRoomId(), newUser.getUsername(), newUser.getSession().getId());

        for (UserSession participant : room.getParticipants()) {
            SendService.sendMessage(participant.getSession(), message);
        }
    }

    private void sendExistingParticipantsList(UserSession user) {
        Room room = getRoomById(user.getRoomId());
        JsonArray participantArray = new JsonArray();

        for (UserSession participant : room.getParticipants()) {
            if (!participant.equals(user)) {
                JsonObject participantInfo = MessageCreator.createParticipantInfo(participant);
                JsonElement jsonElement = new JsonPrimitive(participantInfo.toString());
                participantArray.add(jsonElement);
            }
        }

        JsonObject message = MessageCreator.createParticipantListMessage(user, participantArray, room);
        log.debug("참가자 {} / {} 에게 {}명의 기존 참가자 목록 전송", user.getUsername(), user.getSession().getId(), participantArray.size());

        SendService.sendMessage(user.getSession(), message);
    }

    public Room getRoomById(String roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 방입니다. ID: " + roomId);
        }
        return room;
    }
}