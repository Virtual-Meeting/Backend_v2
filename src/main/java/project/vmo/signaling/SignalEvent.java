package project.vmo.signaling;

import lombok.Getter;

@Getter
public enum SignalEvent {
    CREATE_ROOM("createRoom"),
    JOIN_ROOM("joinRoom"),
    ICE_CANDIDATE("onIceCandidate"),
    RECEIVE_VIDEO("receiveVideoAnswer");

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