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

    public static JsonObject createParticipantInfo(UserSession user) {
        JsonObject participantInfo = new JsonObject();
        participantInfo.addProperty("sessionId", user.getSession().getId());
        participantInfo.addProperty("username", user.getUsername());
        participantInfo.addProperty("audioOn", user.getIsAudioOn());
        participantInfo.addProperty("videoOn", user.getIsVideoOn());
        return participantInfo;
    }

    public static JsonObject createParticipantListMessage(UserSession user, JsonArray participants, Room room) {
        JsonObject participantListMessage = new JsonObject();
        participantListMessage.addProperty("action", "sendExistingUsers");
        participantListMessage.addProperty("sessionId", user.getSession().getId());
        participantListMessage.addProperty("username", user.getUsername());
        participantListMessage.add("participants", participants);
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

    public static JsonObject createReceiveVideoMessage(UserSession sender, String sdpAnswer) {
        final JsonObject receiveVideoMessage = new JsonObject();
        receiveVideoMessage.addProperty("action", "receiveVideoAnswer");
        receiveVideoMessage.addProperty("sessionId", sender.getSession().getId());
        receiveVideoMessage.addProperty("username", sender.getUsername());
        receiveVideoMessage.addProperty("sdpAnswer", sdpAnswer);
        return receiveVideoMessage;
    }

    public static JsonObject createEmojiMessage(UserSession senderSession, String emoji, UserSession receiverSession) {
        JsonObject emojiMessage = new JsonObject();
        emojiMessage.addProperty("action", "sendPublicEmoji");
        emojiMessage.addProperty("senderSessionId", senderSession.getSession().getId());
        emojiMessage.addProperty("senderName", senderSession.getUsername());
        emojiMessage.addProperty("receiverSessionId", receiverSession.getSession().getId());
        emojiMessage.addProperty("receiverName", receiverSession.getUsername());
        emojiMessage.addProperty("emoji", emoji);
        return emojiMessage;
    }

    public static JsonObject createBroadcastChatMessage(UserSession senderSession, String message) {
        JsonObject chatMessage = new JsonObject();
        chatMessage.addProperty("action", "broadcastChat");
        chatMessage.addProperty("senderSessionId", senderSession.getSession().getId());
        chatMessage.addProperty("senderName", senderSession.getUsername());
        chatMessage.addProperty("message", message);
        return chatMessage;
    }

    public static JsonObject createPersonalChatMessage(UserSession senderSession, UserSession receiverSession, String message) {
        JsonObject chatMessage = new JsonObject();
        chatMessage.addProperty("action", "sendPersonalChat");
        chatMessage.addProperty("senderSessionId", senderSession.getSession().getId());
        chatMessage.addProperty("senderName", senderSession.getUsername());
        chatMessage.addProperty("receiverSessionId", receiverSession.getSession().getId());
        chatMessage.addProperty("receiverName", receiverSession.getUsername());
        chatMessage.addProperty("message", message);
        return chatMessage;
    }

    public static JsonObject createPermissionRequestMessage(String sessionId) {
        JsonObject permissionRequestMessage = new JsonObject();
        permissionRequestMessage.addProperty("action", "requestRecordingPermission");
        permissionRequestMessage.addProperty("sessionId", sessionId);
        return permissionRequestMessage;
    }

    public static JsonObject createGrantPermissionMessage(String sessionId) {
        JsonObject grantPermissionMessage = new JsonObject();
        grantPermissionMessage.addProperty("action", "grantPermissionMessage");
        grantPermissionMessage.addProperty("sessionId", sessionId);
        return grantPermissionMessage;
    }

    public static JsonObject createDenyPermissionMessage(String sessionId) {
        JsonObject denyPermissionMessage = new JsonObject();
        denyPermissionMessage.addProperty("action", "denyPermissionMessage");
        denyPermissionMessage.addProperty("sessionId", sessionId);
        return denyPermissionMessage;
    }

    public static JsonObject createStopRecordingMessage(String action, String fileName) {
        JsonObject stopRecordingMessage = new JsonObject();
        stopRecordingMessage.addProperty("action", action);
        stopRecordingMessage.addProperty("fileName", fileName);
        return stopRecordingMessage;
    }

    public static JsonObject createVideoStateChangeMessage(String action, String sessionId, Boolean videoState) {
        JsonObject videoStateChangeMessage = new JsonObject();
        videoStateChangeMessage.addProperty("action", action);
        videoStateChangeMessage.addProperty("sessionId", sessionId);
        videoStateChangeMessage.addProperty("videoOn", videoState);
        return videoStateChangeMessage;
    }

    public static JsonObject createAudioStateChangeMessage(String action, String sessionId, Boolean audioState) {
        JsonObject audioStateChangeMessage = new JsonObject();
        audioStateChangeMessage.addProperty("action", action);
        audioStateChangeMessage.addProperty("sessionId", sessionId);
        audioStateChangeMessage.addProperty("audioOn", audioState);
        return audioStateChangeMessage;
    }

    public static JsonObject createChangeUsername(String sessionId, String newUsername) {
        JsonObject usernameChangeMessage = new JsonObject();
        usernameChangeMessage.addProperty("action", "changeName");
        usernameChangeMessage.addProperty("sessionId", sessionId);
        usernameChangeMessage.addProperty("newUserName", newUsername);
        return usernameChangeMessage;
    }

    public static JsonObject simple(String action) {
        JsonObject message = new JsonObject();
        message.addProperty("action", action);
        return message;
    }

    public static JsonObject createErrorMessage(RuntimeException e) {
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