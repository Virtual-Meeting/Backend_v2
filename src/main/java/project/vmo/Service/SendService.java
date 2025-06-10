package project.vmo.service;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.domain.Room;
import project.vmo.domain.UserSession;
import project.vmo.util.MessageCreator;

import java.io.IOException;
import java.util.Collection;

@Service
public class SendService {
    private static final Logger log = LoggerFactory.getLogger(SendService.class);
    private final RoomService roomService;

    public SendService(RoomService roomService) {
        this.roomService = roomService;
    }

    public static void sendMessage(WebSocketSession session, JsonObject message) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message.toString()));
                log.info("전송한 메시지: {}", message);
            }
        } catch (IOException e) {
            log.warn("메시지 전송 실패: {}", e.getMessage());
        }
    }

    public void sendPublicEmoji(UserSession senderSession, String emoji) {
        Room room = roomService.getRoomById(senderSession.getRoomId());
        Collection<UserSession> receiverSessions = room.getParticipants();

        receiverSessions.forEach(receiverSession -> {
            JsonObject emojiMessage = MessageCreator.createEmojiMessage(senderSession, emoji, receiverSession);
            sendMessage(receiverSession.getSession(), emojiMessage);
        });
    }

    public void broadcastChat(UserSession senderSession, String message) {
        Room room = roomService.getRoomById(senderSession.getRoomId());
        Collection<UserSession> receiverSessions = room.getParticipants();

        JsonObject chatMessage = MessageCreator.createBroadcastChatMessage(senderSession, message);

        receiverSessions.forEach(userSession -> {
            if (!userSession.getSession().getId().equals(senderSession.getSession().getId())) {
                sendMessage(userSession.getSession(), chatMessage);
            }
        });
    }

    public void sendPersonalChat(UserSession senderSession, UserSession receiverSession, String message) {
        JsonObject chatMessage = MessageCreator.createPersonalChatMessage(senderSession, receiverSession, message);

        sendMessage(receiverSession.getSession(), chatMessage);
    }
}