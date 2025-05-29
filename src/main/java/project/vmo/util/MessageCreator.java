package project.vmo.util;

import com.google.gson.JsonObject;
import project.vmo.domain.UserSession;
import project.vmo.dto.CreateRoomDto;
import project.vmo.domain.Room;

public class MessageCreator {
    public static JsonObject createRoomCreatedMessage(CreateRoomDto dto, UserSession user, Room room) {
        JsonObject roomCreatedMessage = new JsonObject();
        roomCreatedMessage.addProperty("action", "roomCreated");
        roomCreatedMessage.addProperty("sessionId", user.getSession().getId());
        roomCreatedMessage.addProperty("username", user.getUsername());
        roomCreatedMessage.addProperty("roomId", room.getRoomId());
        roomCreatedMessage.addProperty("roomLeaderId", room.getLeaderSessionId());
        roomCreatedMessage.addProperty("roomLeaderName", dto.username());
        roomCreatedMessage.addProperty("audioOn", user.getIsAudioOn());
        roomCreatedMessage.addProperty("videoOn", user.getIsVideoOn());
        return roomCreatedMessage;
    }
}
