package project.vmo.service;

import org.kurento.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import project.vmo.permission.PermissionRepository;
import project.vmo.controller.RecordingDownloadController;
import project.vmo.domain.Room;
import project.vmo.domain.UserSession;
import project.vmo.signaling.SignalEvent;
import project.vmo.util.MessageCreator;

import java.io.File;

@Service
public class RecordingService {
    private static final Logger log = LoggerFactory.getLogger(RecordingService.class);

    private final RoomService roomService;
    private final PermissionRepository permissionRepository;

    public RecordingService(RoomService roomService, PermissionRepository permissionRepository) {
        this.roomService = roomService;
        this.permissionRepository = permissionRepository;
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

        permissionRepository.removePermittedUser(userSession.getSession().getId());
    }

    public void pauseRecording(UserSession userSession) {
        RecorderEndpoint recorder = userSession.getRecorderEndpoint();
        if (recorder != null) {
            recorder.pause();
            log.info("녹화 일시 정지: {}", userSession.getUsername());
        }

        SendService.sendMessage(userSession.getSession(), MessageCreator.simple(SignalEvent.PAUSE_RECORDING.getValue()));
    }

    public void resumeRecording(UserSession userSession) {
        RecorderEndpoint recorder = userSession.getRecorderEndpoint();
        if (recorder != null) {
            recorder.record();
            log.info("녹화 재개: {}", userSession.getUsername());
        }

        SendService.sendMessage(userSession.getSession(), MessageCreator.simple(SignalEvent.RESUME_RECORDING.getValue()));
    }

    public void grantRecordingPermission(UserSession userSession) {
        String sessionId = userSession.getSession().getId();
        String roomId = userSession.getRoomId();

        validateRecordPermission(sessionId, roomId);

        permissionRepository.addPermittedUser(sessionId, roomId);
        SendService.sendMessage(userSession.getSession(), MessageCreator.createGrantPermissionMessage(sessionId));
    }

    public void validateRecordPermission(String sessionId, String roomId) {
        Room room = roomService.getRoomById(roomId);
        String roomLeaderId = room.getLeaderSessionId();

        if (permissionRepository.checkPermittedUser(sessionId) || sessionId.equals(roomLeaderId)) {
            throw new IllegalStateException("이미 녹화 권한이 부여된 사용자입니다.");
        }

        if (permissionRepository.countPermittedUser(roomId) >= 2) throw new IllegalStateException("녹화 권한은 최대 2명까지 부여할 수 있으며, 현재 이미 2명이 권한을 보유하고 있습니다.");
    }

    public static void deleteRecordings(String roomId) {
        File recordingDir = new File(RecordingDownloadController.RECORDING_DIR + "/" + roomId);
        File convertedDir = new File(RecordingDownloadController.CONVERTED_DIR + "/" + roomId);

        deleteDirectoryRecursively(recordingDir);
        deleteDirectoryRecursively(convertedDir);
    }

    private void checkRecordingPermission(UserSession userSession) {
        Room room = roomService.getRoomById(userSession.getRoomId());
        String roomLeaderId = room.getLeaderSessionId();

        if (!userSession.getSession().getId().equals(roomLeaderId) && !isPermittedUser(userSession)) {
            throw new IllegalStateException(userSession.getUsername() + "는 녹화 권한이 없는 사용자입니다.");
        }
    }

    private boolean isPermittedUser(UserSession userSession) {
        Room room = roomService.getRoomById(userSession.getRoomId());
        return room.getLeaderSessionId().equals(userSession.getSession().getId()) || permissionRepository.checkPermittedUser(userSession.getSession().getId());
    }

    private static void deleteDirectoryRecursively(File dir) {
        if (dir.exists()) {
            File[] contents = dir.listFiles();
            if (contents != null) {
                for (File file : contents) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}