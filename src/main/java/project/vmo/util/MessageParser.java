package project.vmo.util;

import com.google.gson.JsonObject;
import org.kurento.client.IceCandidate;
import project.vmo.dto.IceCandidateDto;

public class MessageParser {
    public static IceCandidateDto parseIceCandidateRequest(JsonObject iceCandidateRequest) {
        String sessionId = iceCandidateRequest.get("sessionId").getAsString();
        JsonObject candidateInfo = iceCandidateRequest.get("candidate").getAsJsonObject();
        IceCandidate iceCandidate = new IceCandidate(candidateInfo.get("candidate").getAsString(),
                candidateInfo.get("sdpMid").getAsString(), candidateInfo.get("sdpMLineIndex").getAsInt());

        return new IceCandidateDto(sessionId, iceCandidate);
    }
}