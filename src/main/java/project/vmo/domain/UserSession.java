package project.vmo.domain;

import lombok.Getter;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.Service.IceCandidateService;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserSession implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    @Getter
    private final String username;
    @Getter
    private final String roomId;
    @Getter
    private final WebSocketSession session;
    @Getter
    private final Boolean isAudioOn;
    @Getter
    private final Boolean isVideoOn;

    @Getter
    private final MediaPipeline pipeline;
    @Getter
    private final WebRtcEndpoint outgoingMedia;

    private final ConcurrentMap<String, WebRtcEndpoint> incomingMediaBySessionId = new ConcurrentHashMap<>();

    public UserSession(String username, String roomId,
                       Boolean isAudioOn, Boolean isVideoOn,
                       WebSocketSession session, MediaPipeline pipeline,
                       WebRtcEndpoint outgoingMedia,
                       IceCandidateService iceCandidateService) {

        this.username = username;
        this.roomId = roomId;
        this.session = session;
        this.isAudioOn = isAudioOn;
        this.isVideoOn = isVideoOn;
        this.pipeline = pipeline;
        this.outgoingMedia = outgoingMedia;
        iceCandidateService.attachCandidateListener(outgoingMedia, session, username);
    }

    public WebRtcEndpoint getIncomingMedia(String sessionId) {
        return incomingMediaBySessionId.get(sessionId);
    }

    public void addIncomingMedia(String sessionId, WebRtcEndpoint incomingMedia) {
        incomingMediaBySessionId.put(sessionId, incomingMedia);
    }

    @Override
    public void close() throws IOException {
        for (final String remoteParticipantSessionId : incomingMediaBySessionId.keySet()) {

            log.trace("사용자 {} / {}: {} 참가자에 대한 수신 WebRTC 연결 해제 시도",
                    this.username, this.session.getId(), remoteParticipantSessionId);

            final WebRtcEndpoint ep = this.incomingMediaBySessionId.get(remoteParticipantSessionId);

            ep.release(new Continuation<Void>() {

                @Override
                public void onSuccess(Void result) throws Exception {
                    log.trace("사용자 {} / {}: {} 참가자 수신 연결 정상 해제 완료",
                            username, session.getId(), remoteParticipantSessionId);
                }

                @Override
                public void onError(Throwable cause) throws Exception {
                    log.warn("사용자 {} / {}: {} 참가자 수신 연결 해제 실패",
                            username, session.getId(), remoteParticipantSessionId);
                }
            });
        }

        outgoingMedia.release(new Continuation<Void>() {

            @Override
            public void onSuccess(Void result) throws Exception {
                log.trace("사용자 {} / {}: 송신 WebRTC 연결 정상 해제 완료", username, session.getId());
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("사용자 {} / {}: 송신 WebRTC 연결 해제 실패", username, session.getId());
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof UserSession other)) {
            return false;
        }

        boolean eq = session.getId().equals(other.session.getId());
        eq &= roomId.equals(other.roomId);
        return eq;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + session.getId().hashCode();
        result = 31 * result + roomId.hashCode();
        return result;
    }
}