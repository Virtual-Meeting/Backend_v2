package project.vmo.service;

import com.google.gson.JsonObject;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.util.MessageCreator;
import project.vmo.domain.UserSession;
import project.vmo.dto.IceCandidateDto;

import java.io.IOException;

@Service
public class IceCandidateService {
    private static final Logger log = LoggerFactory.getLogger(IceCandidateService.class);

    public void attachCandidateListener(WebRtcEndpoint endpoint, WebSocketSession session, String username) {
        endpoint.addIceCandidateFoundListener(event -> {
            JsonObject candidateMessage = MessageCreator.createCandidateMessage(event, session, username);
            log.debug("사용자 [{}]에 대한 ICE 후보가 발견되었습니다: {}", username, candidateMessage);
            SendService.sendMessage(session, candidateMessage);
        });
    }

    public void addCandidate(UserSession userSession, IceCandidateDto iceCandidateDto) throws IOException {
        String currentSessionId = userSession.getSession().getId();
        String targetSessionId = iceCandidateDto.sessionId();

        if (currentSessionId.equals(targetSessionId)) {
            log.debug("세션 [{}]의 송신 미디어에 ICE 후보를 추가합니다.", currentSessionId);
            userSession.getOutgoingMedia().addIceCandidate(iceCandidateDto.iceCandidate());
        } else {
            WebRtcEndpoint incomingMedia = userSession.getIncomingMedia(targetSessionId);
            if (incomingMedia != null) {
                log.debug("세션 [{}]에서 [{}]로 수신 미디어에 ICE 후보를 추가합니다.", targetSessionId, currentSessionId);
                incomingMedia.addIceCandidate(iceCandidateDto.iceCandidate());
            } else {
                log.warn("세션 [{}]에 대한 수신 미디어가 존재하지 않아 ICE 후보를 추가할 수 없습니다. (대상 사용자 세션: [{}])", targetSessionId, currentSessionId);
            }
        }
    }
}