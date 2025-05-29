package project.vmo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public record CreateRoomDto(@NotBlank(message = "사용자 이름은 공백이거나 비어 있을 수 없습니다.") String username,
                            @NotNull(message = "오디오 상태 값은 필수입니다.") Boolean audioState,
                            @NotNull(message = "비디오 상태 값은 필수입니다.") Boolean videoState) {

    public CreateRoomDto(String username, Boolean audioState, Boolean videoState) {
        this.username = username;
        this.audioState = audioState;
        this.videoState = videoState;
    }
}