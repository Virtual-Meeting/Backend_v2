package project.vmo.signaling;

import project.vmo.Service.SendService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import project.vmo.domain.UserSession;
import project.vmo.dto.CreateRoomDto;
import project.vmo.Service.RoomService;
import project.vmo.session.SessionRepository;
import project.vmo.util.MessageCreator;

import java.io.IOException;

@Component
public class SignalHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(SignalHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final SessionRepository sessionRegistry;
    private final RoomService roomService;

    public SignalHandler(SessionRepository sessionRegistry, RoomService roomService) {
        this.sessionRegistry = sessionRegistry;
        this.roomService = roomService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        log.trace("수신된 메시지: {}", message.getPayload());

        try {
            dispatchSignalEvent(session, message);
        } catch (IllegalArgumentException e) {
            handleErrorAndNotify(session, e);
        }
    }

    private void dispatchSignalEvent(WebSocketSession session, TextMessage message) throws IOException {
        final JsonObject requestMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        String eventId = requestMessage.get("eventId").getAsString();
        SignalEvent signalEvent = SignalEvent.from(eventId);

        switch (signalEvent) {
            case CREATE_ROOM:
                handleCreateRoom(session, requestMessage);
                break;
            default:
                break;
        }
    }

    private static void handleErrorAndNotify(WebSocketSession session, IllegalArgumentException e) {
        log.error(e.getMessage());

        JsonObject errorMessage = MessageCreator.createErrorMessage(e);
        SendService.sendMessage(session, errorMessage);
    }

    private void handleCreateRoom(WebSocketSession session, JsonObject jsonMessage) {
        CreateRoomDto createRoomDto = gson.fromJson(jsonMessage, CreateRoomDto.class);
        UserSession newUserSession = roomService.createRoom(session, createRoomDto);
        sessionRegistry.register(newUserSession);
    }
}