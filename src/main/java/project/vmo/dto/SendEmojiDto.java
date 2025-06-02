package project.vmo.dto;

import jakarta.validation.constraints.NotNull;
import project.vmo.domain.UserSession;

public record SendEmojiDto(@NotNull(message = "User Session 값은 필수입니다.") UserSession senderSession,
                           @NotNull(message = "이모지는 필수 입력 값입니다.") String emoji) {

    public SendEmojiDto(UserSession senderSession, String emoji) {
        this.senderSession = senderSession;
        this.emoji = emoji;
    }
}