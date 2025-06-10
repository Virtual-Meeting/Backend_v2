package project.vmo.service;

import com.google.gson.JsonObject;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import project.vmo.util.MessageCreator;
import project.vmo.domain.UserSession;

import java.io.IOException;

@Service
public class ReceiveVideoService {
    private static final Logger log = LoggerFactory.getLogger(ReceiveVideoService.class);

    public void receiveVideo(UserSession receiverSession, UserSession senderSession, JsonObject jsonMessage) {
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        log.info("사용자 [{} / {}]가 [{}]와 연결을 시도합니다. (방 ID: {})",
                receiverSession.getUsername(),
                receiverSession.getSession().getId(),
                senderSession.getUsername(),
                receiverSession.getRoomId()
        );

        log.trace("사용자 [{} / {}]가 [{}]로부터 받은 SDP Offer: {}",
                receiverSession.getUsername(),
                receiverSession.getSession().getId(),
                senderSession.getUsername(),
                sdpOffer
        );

        WebRtcEndpoint endpoint = getOrCreateEndpointBetween(receiverSession, senderSession);
        final String sdpAnswer = endpoint.processOffer(sdpOffer);

        JsonObject receiveVideoMessage = MessageCreator.createReceiveVideoMessage(senderSession, sdpAnswer);

        log.trace("사용자 [{} / {}]가 [{}]에게 보낼 SDP Answer: {}",
                receiverSession.getUsername(),
                receiverSession.getSession().getId(),
                senderSession.getUsername(),
                sdpAnswer
        );

        SendService.sendMessage(receiverSession.getSession(), receiveVideoMessage);
        log.debug("ICE 후보 수집을 시작합니다.");
        endpoint.gatherCandidates();
    }

    private WebRtcEndpoint getOrCreateEndpointBetween(UserSession receiverSession, UserSession senderSession) {
        if (receiverSession.getSession().getId().equals(senderSession.getSession().getId())) {
            log.debug("사용자 [{} / {}]의 루프백 미디어를 구성합니다.",
                    receiverSession.getUsername(),
                    receiverSession.getSession().getId()
            );
            return receiverSession.getOutgoingMedia();
        }

        log.debug("사용자 [{} / {}]가 [{} / {}]의 비디오를 수신합니다.",
                receiverSession.getUsername(), receiverSession.getSession().getId(),
                senderSession.getUsername(), senderSession.getSession().getId()
        );

        WebRtcEndpoint incomingEndpoint = receiverSession.getIncomingMedia(senderSession.getSession().getId());
        if (incomingEndpoint == null) {
            incomingEndpoint = new WebRtcEndpoint.Builder(receiverSession.getPipeline()).build();

            incomingEndpoint.addIceCandidateFoundListener(event -> {
                JsonObject candidateMessage = MessageCreator.createCandidateMessage(event, senderSession);
                try {
                    synchronized (receiverSession.getSession()) {
                        receiverSession.getSession().sendMessage(new TextMessage(candidateMessage.toString()));
                    }
                } catch (IOException e) {
                    log.warn("ICE 후보 메시지 전송 중 예외 발생: {}", e.getMessage());
                    JsonObject errorMessage = MessageCreator.createErrorMessage(e);
                    SendService.sendMessage(receiverSession.getSession(), errorMessage);
                }
            });

            receiverSession.addIncomingMedia(senderSession.getSession().getId(), incomingEndpoint);
        }

        senderSession.getOutgoingMedia().connect(incomingEndpoint);

        log.debug("사용자 [{} / {}]가 [{} / {}]에 대한 WebRtcEndpoint 연결을 완료했습니다.",
                receiverSession.getUsername(), receiverSession.getSession().getId(),
                senderSession.getUsername(), senderSession.getSession().getId()
        );

        return incomingEndpoint;
    }
}