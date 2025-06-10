package project.vmo.service;

import org.kurento.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import project.vmo.domain.Room;
import project.vmo.domain.UserSession;
import project.vmo.signaling.SignalEvent;
import project.vmo.util.MessageCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordingService {
    private static final Logger log = LoggerFactory.getLogger(RecordingService.class);

    private final RoomService roomService;

    private final Map<String, String> permittedUser = new HashMap<>();

    public RecordingService(RoomService roomService) {
        this.roomService = roomService;
    }

    public void startRecording(UserSession userSession, String filePath) {
        checkRecordingPermission(userSession);

        MediaPipeline pipeline = userSession.getPipeline();
        WebRtcEndpoint outgoingMedia = userSession.getOutgoingMedia();

        RecorderEndpoint recorder = new RecorderEndpoint.Builder(pipeline, "file://" + filePath)
                .withMediaProfile(MediaProfileSpecType.WEBM)
                .build();

        outgoingMedia.connect(recorder);
        recorder.record();

        userSession.setRecorderEndpoint(recorder);

        log.info("녹화 시작: {} -> {}", userSession.getUsername(), filePath);

        SendService.sendMessage(userSession.getSession(), MessageCreator.simple(SignalEvent.START_RECORDING.getValue()));
    }

    public void stopRecording(UserSession userSession) {
        checkRecordingPermission(userSession);

        RecorderEndpoint recorder = userSession.getRecorderEndpoint();

        if (recorder != null) {
            recorder.stop();
            log.info("녹화 중지: {}", userSession.getUsername());
            SendService.sendMessage(userSession.getSession(), MessageCreator.createStopRecordingMessage(SignalEvent.STOP_RECORDING.getValue(), recorder.getName()));
        }

        deleteRecordingPermission(userSession.getSession().getId());
    }

    public void pauseRecording(UserSession userSession) {
        checkRecordingPermission(userSession);

        RecorderEndpoint recorder = userSession.getRecorderEndpoint();
        if (recorder != null) {
            recorder.pause();
            log.info("녹화 일시 정지: {}", userSession.getUsername());
        }

        SendService.sendMessage(userSession.getSession(), MessageCreator.simple(SignalEvent.PAUSE_RECORDING.getValue()));
    }

    public void grantRecordingPermission(UserSession userSession) {
        String sessionId = userSession.getSession().getId();
        String roomId = userSession.getRoomId();

        validateRecordPermission(sessionId, roomId);

        permittedUser.put(sessionId, roomId);
        SendService.sendMessage(userSession.getSession(), MessageCreator.createGrantPermissionMessage(sessionId));
    }

    public void validateRecordPermission(String sessionId, String roomId) {
        Room room = roomService.getRoomById(roomId);
        String roomLeaderId = room.getLeaderSessionId();

        if (permittedUser.containsKey(sessionId) || sessionId.equals(roomLeaderId)) {
            throw new IllegalStateException("이미 녹화 권한이 부여된 사용자입니다.");
        }

        long count = permittedUser.values().stream()
                .filter(id -> id.equals(roomId))
                .count();

        if (count >= 2) throw new IllegalStateException("녹화 권한은 최대 2명까지 부여할 수 있으며, 현재 이미 2명이 권한을 보유하고 있습니다.");
    }

    private void checkRecordingPermission(UserSession userSession) {
        Room room = roomService.getRoomById(userSession.getRoomId());
        String roomLeaderId = room.getLeaderSessionId();

        if (!userSession.getSession().getId().equals(roomLeaderId) && !isPermittedUser(userSession)) {
            throw new IllegalStateException(userSession.getUsername() + "는 녹화 권한이 없는 사용자입니다.");
        }
    }

    private void deleteRecordingPermission(String sessionId) {
        permittedUser.remove(sessionId);
    }

    private boolean isPermittedUser(UserSession userSession) {
        Room room = roomService.getRoomById(userSession.getRoomId());
        return room.getLeaderSessionId().equals(userSession.getSession().getId()) || permittedUser.containsKey(userSession.getSession().getId());
    }
}