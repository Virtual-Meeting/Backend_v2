package project.vmo.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.domain.UserSession;
import project.vmo.dto.CreateRoomDto;
import project.vmo.domain.Room;

import java.io.IOException;

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

    public static JsonObject createNewUserJoinedMessage(UserSession user) {
        JsonObject newUserJoinedMessage = new JsonObject();
        newUserJoinedMessage.addProperty("action", "newUserJoined");
        newUserJoinedMessage.addProperty("sessionId", user.getSession().getId());
        newUserJoinedMessage.addProperty("username", user.getUsername());
        newUserJoinedMessage.addProperty("audioOn", user.getIsAudioOn());
        newUserJoinedMessage.addProperty("videoOn", user.getIsVideoOn());
        return newUserJoinedMessage;
    }

    public static JsonObject createParticipantInfoMessage(UserSession user) {
        JsonObject participantInfo = new JsonObject();
        participantInfo.addProperty("sessionId", user.getSession().getId());
        participantInfo.addProperty("username", user.getUsername());
        participantInfo.addProperty("audioOn", user.getIsAudioOn());
        participantInfo.addProperty("videoOn", user.getIsVideoOn());
        return participantInfo;
    }

    public static JsonObject createParticipantListMessage(UserSession user, JsonArray array, Room room) {
        JsonObject participantListMessage = new JsonObject();
        participantListMessage.addProperty("action", "sendExistingUsers");
        participantListMessage.addProperty("sessionId", user.getSession().getId());
        participantListMessage.addProperty("username", user.getUsername());
        participantListMessage.add("participants", array);
        participantListMessage.addProperty("roomLeaderId", room.getLeaderSessionId());
        participantListMessage.addProperty("roomLeaderName", room.getLeaderName());
        participantListMessage.addProperty("audioOn", user.getIsAudioOn());
        participantListMessage.addProperty("videoOn", user.getIsVideoOn());
        return participantListMessage;
    }

    public static JsonObject createCandidateMessage(IceCandidateFoundEvent event, WebSocketSession session, String username) {
        JsonObject candidateMessage = new JsonObject();
        candidateMessage.addProperty("action", "onIceCandidate");
        candidateMessage.addProperty("sessionId", session.getId());
        candidateMessage.addProperty("username", username);
        candidateMessage.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        return candidateMessage;
    }

    public static JsonObject createCandidateMessage(IceCandidateFoundEvent event, UserSession sender) {
        JsonObject candidateMessage = new JsonObject();
        candidateMessage.addProperty("action", "onIceCandidate");
        candidateMessage.addProperty("sessionId", sender.getSession().getId());
        candidateMessage.addProperty("username", sender.getUsername());
        candidateMessage.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        return candidateMessage;
    }

    public static JsonObject createReceiveVideoMessage(UserSession sender, String ipSdpAnswer) {
        final JsonObject receiveVideoMessage = new JsonObject();
        receiveVideoMessage.addProperty("action", "receiveVideoFrom");
        receiveVideoMessage.addProperty("sessionId", sender.getSession().getId());
        receiveVideoMessage.addProperty("username", sender.getUsername());
        receiveVideoMessage.addProperty("sdpAnswer", ipSdpAnswer);
        return receiveVideoMessage;
    }

    public static JsonObject createErrorMessage(IllegalArgumentException e) {
        JsonObject errorMessage = new JsonObject();
        errorMessage.addProperty("type", "ERROR");
        errorMessage.addProperty("message", e.getMessage());
        return errorMessage;
    }

    public static JsonObject createErrorMessage(IOException e) {
        JsonObject errorMessage = new JsonObject();
        errorMessage.addProperty("type", "ERROR");
        errorMessage.addProperty("message", e.getMessage());
        return errorMessage;
    }
}