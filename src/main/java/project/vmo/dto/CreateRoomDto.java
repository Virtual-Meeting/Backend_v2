package project.vmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomDto(@NotBlank(message = "사용자 이름은 공백이거나 비어 있을 수 없습니다.") String username,
                            @NotNull(message = "오디오 상태 값은 필수입니다.") Boolean audioOn,
                            @NotNull(message = "비디오 상태 값은 필수입니다.") Boolean videoOn) {

    public CreateRoomDto(String username, Boolean audioOn, Boolean videoOn) {
        this.username = username;
        this.audioOn = audioOn;
        this.videoOn = videoOn;
    }
}