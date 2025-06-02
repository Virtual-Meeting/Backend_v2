package project.vmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record JoinRoomDto(@NotBlank(message = "사용자 이름은 공백이거나 비어 있을 수 없습니다.") String username,
                          @NotNull(message = "방 ID는 필수 입력 값입니다.") @Pattern(regexp = "\\d{6}", message = "방 ID는 6자리 숫자여야 합니다.") String roomId,
                          @NotNull(message = "오디오 상태 값은 필수입니다.") Boolean audioOn,
                          @NotNull(message = "비디오 상태 값은 필수입니다.") Boolean videoOn) {

    public JoinRoomDto(String username, String roomId, Boolean audioOn, Boolean videoOn) {
        this.username = username;
        this.roomId = roomId;
        this.audioOn = audioOn;
        this.videoOn = videoOn;
    }
}