package project.vmo.Service;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class SendService {
    private static final Logger log = LoggerFactory.getLogger(SendService.class);

    public static void sendMessage(WebSocketSession session, JsonObject message) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message.toString()));
            }
        } catch (IOException e) {
            log.warn("메시지 전송 실패: {}", e.getMessage());
        }
    }
}