package project.vmo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.kurento.client.IceCandidate;

@Getter
public record IceCandidateDto(@NotNull(message = "사용자 세션 ID 값은 필수입니다.") String sessionId,
                              @NotNull(message = "ice candidate 값은 필수입니다.") IceCandidate iceCandidate) {

    public IceCandidateDto(String sessionId, IceCandidate iceCandidate) {
        this.sessionId = sessionId;
        this.iceCandidate = iceCandidate;
    }
}