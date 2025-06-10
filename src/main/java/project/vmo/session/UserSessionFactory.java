package project.vmo.session;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.service.IceCandidateService;
import project.vmo.domain.UserSession;

@Component
public class UserSessionFactory {
    private final IceCandidateService iceCandidateService;

    public UserSessionFactory(IceCandidateService iceCandidateService) {
        this.iceCandidateService = iceCandidateService;
    }

    public UserSession create(String username, String roomId,
                              Boolean isAudioOn, Boolean isVideoOn,
                              WebSocketSession session, MediaPipeline pipeline) {

        WebRtcEndpoint outgoing = new WebRtcEndpoint.Builder(pipeline).build();

        return new UserSession(username, roomId, isAudioOn, isVideoOn, session, pipeline, outgoing, iceCandidateService);
    }
}