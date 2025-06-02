package project.vmo.signaling;

import lombok.Getter;

@Getter
public enum SignalEvent {
    CREATE_ROOM("createRoom"),
    JOIN_ROOM("joinRoom"),
    EXIT_ROOM("exitRoom"),
    ICE_CANDIDATE("onIceCandidate"),
    RECEIVE_VIDEO("receiveVideoAnswer"),
    SEND_EMOJI("sendPublicEmoji"),
    BROADCAST_CHAT("broadcastChat"),
    SEND_PERSONAL_CHAT("sendPersonalChat"),
    CHANGE_NAME("changeName"),
    AUDIO_STATE_CHANGE("audioStateChange"),
    VIDEO_STATE_CHANGE("videoStateChange"),
    START_RECORDING("startRecording"),
    STOP_RECORDING("stopRecording"),
    SAVE_RECORDING("saveRecording"),
    PAUSE_RECORDING("pauseRecording"),
    RESUME_RECORDING("resumeRecording"),
    REQUEST_RECORDING_PERMISSION("requestRecordingPermission"),
    GRANT_RECORDING_PERMISSION("grantRecordingPermission"),
    DENY_RECORDING_PERMISSION("denyRecordingPermission");

    private final String value;

    SignalEvent(String value) {
        this.value = value;
    }

    public static SignalEvent from(String value) {
        for (SignalEvent event : values()) {
            if (event.getValue().equals(value)) {
                return event;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 이벤트입니다: event id = " + value);
    }
}