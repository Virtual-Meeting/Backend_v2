package project.vmo.signaling;

import project.vmo.controller.RecordingDownloadController;
import project.vmo.service.*;
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
import project.vmo.dto.IceCandidateDto;
import project.vmo.dto.CreateRoomDto;
import project.vmo.dto.JoinRoomDto;
import project.vmo.session.SessionRepository;
import project.vmo.util.MessageCreator;
import project.vmo.util.MessageParser;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SignalHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(SignalHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final SessionRepository sessionRegistry;
    private final IceCandidateService iceCandidateService;
    private final ReceiveVideoService receiveVideoService;
    private final RoomService roomService;
    private final SendService sendService;
    private final RecordingService recordingService;

    public SignalHandler(SessionRepository sessionRegistry, IceCandidateService iceCandidateService,
                         ReceiveVideoService receiveVideoService, RoomService roomService,
                         SendService sendService, RecordingService recordingService) {
        this.sessionRegistry = sessionRegistry;
        this.iceCandidateService = iceCandidateService;
        this.receiveVideoService = receiveVideoService;
        this.roomService = roomService;
        this.sendService = sendService;
        this.recordingService = recordingService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        log.trace("수신된 메시지: {}", message.getPayload());

        try {
            dispatchSignalEvent(session, message);
        } catch (IllegalArgumentException | IllegalStateException e) {
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
            case JOIN_ROOM:
                handleJoinRoom(session, requestMessage);
                break;
            case ICE_CANDIDATE:
                handleIceCandidate(session, requestMessage);
                break;
            case RECEIVE_VIDEO:
                handleReceiveVideo(session, requestMessage);
                break;
            case SEND_EMOJI:
                handleSendEmoji(session, requestMessage);
                break;
            case BROADCAST_CHAT:
                handleBroadcastChat(session, requestMessage);
                break;
            case SEND_PERSONAL_CHAT:
                handleSendPersonalChat(session, requestMessage);
                break;
            case START_RECORDING:
                handleStartRecording(session);
                break;
            case STOP_RECORDING:
                handleStopRecording(session);
                break;
            case PAUSE_RECORDING:
                handlePauseRecording(session);
                break;
            case AUDIO_STATE_CHANGE:
                handleAudioStateChange(session, requestMessage);
                break;
            case VIDEO_STATE_CHANGE:
                handleVideoStateChange(session, requestMessage);
                break;
            default:
                break;
        }
    }

    private static void handleErrorAndNotify(WebSocketSession session, RuntimeException e) {
        log.error(e.getMessage());

        JsonObject errorMessage = MessageCreator.createErrorMessage(e);
        SendService.sendMessage(session, errorMessage);
    }

    private void handleCreateRoom(WebSocketSession session, JsonObject jsonMessage) {
        CreateRoomDto createRoomDto = gson.fromJson(jsonMessage, CreateRoomDto.class);
        UserSession newUserSession = roomService.createRoom(session, createRoomDto);
        sessionRegistry.register(newUserSession);
    }

    private void handleJoinRoom(WebSocketSession session, JsonObject jsonMessage) {
        JoinRoomDto joinRoomDto = gson.fromJson(jsonMessage, JoinRoomDto.class);
        UserSession newUserSession = roomService.joinRoom(session, joinRoomDto);
        sessionRegistry.register(newUserSession);
    }

    private void handleIceCandidate(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        UserSession userSession = sessionRegistry.getBySession(session);

        if (userSession != null) {
            IceCandidateDto iceCandidateDto = MessageParser.parseIceCandidateRequest(jsonMessage);
            iceCandidateService.addCandidate(userSession, iceCandidateDto);
        }
    }

    private void handleReceiveVideo(WebSocketSession session, JsonObject jsonMessage) {
        String videoSenderSessionId = jsonMessage.get("sessionId").getAsString();

        UserSession receiver = sessionRegistry.getBySession(session);
        UserSession sender = sessionRegistry.getBySessionId(videoSenderSessionId);

        if (receiver != null) {
            receiveVideoService.receiveVideo(receiver, sender, jsonMessage);
        }
    }

    private void handleSendEmoji(WebSocketSession session, JsonObject jsonMessage) {
        String emoji = jsonMessage.get("emoji").getAsString();
        UserSession senderSession = sessionRegistry.getBySession(session);
        sendService.sendPublicEmoji(senderSession, emoji);
    }

    private void handleBroadcastChat(WebSocketSession session, JsonObject jsonMessage) {
        String message = jsonMessage.get("message").getAsString();
        UserSession senderSession = sessionRegistry.getBySession(session);
        sendService.broadcastChat(senderSession, message);
    }

    private void handleSendPersonalChat(WebSocketSession session, JsonObject jsonMessage) {
        String message = jsonMessage.get("message").getAsString();
        String receiverSessionId = jsonMessage.get("receiverSessionId").getAsString();

        UserSession senderSession = sessionRegistry.getBySession(session);
        UserSession receiverSession = sessionRegistry.getBySessionId(receiverSessionId);
        sendService.sendPersonalChat(senderSession, receiverSession, message);
    }

    private void handleStartRecording(WebSocketSession session) {
        UserSession userSession = sessionRegistry.getBySession(session);

        String fileName = userSession.getRoomId() + "_" + LocalDateTime.now().toString().replace(":", ".");
        String filePath = RecordingDownloadController.RECORDING_DIR + userSession.getRoomId() + "/" + fileName + ".webm";

        recordingService.startRecording(userSession, filePath);
    }

    private void handleStopRecording(WebSocketSession session) {
        UserSession userSession = sessionRegistry.getBySession(session);
        recordingService.stopRecording(userSession);
    }

    private void handlePauseRecording(WebSocketSession session) {
        UserSession userSession = sessionRegistry.getBySession(session);
        recordingService.pauseRecording(userSession);
    }

    private void handleVideoStateChange(WebSocketSession session, JsonObject jsonMessage) {
        Boolean videoState = jsonMessage.get("videoOn").getAsBoolean();
        UserSession userSession = sessionRegistry.getBySession(session);
        userSession.changeVideoState(videoState);
        SendService.sendMessage(session, MessageCreator.createVideoStateChangeMessage(SignalEvent.VIDEO_STATE_CHANGE.getValue(), session.getId(), videoState));
    }

    private void handleAudioStateChange(WebSocketSession session, JsonObject jsonMessage) {
        Boolean audioState = jsonMessage.get("audioOn").getAsBoolean();
        UserSession userSession = sessionRegistry.getBySession(session);
        userSession.changeAudioState(audioState);
        SendService.sendMessage(session, MessageCreator.createAudioStateChangeMessage(SignalEvent.AUDIO_STATE_CHANGE.getValue(), session.getId(), audioState));
    }
}