package project.vmo.util;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
public class RoomIdGenerator {
    private static final Set<String> generatedRoomIds = new HashSet<>();
    private static final Random random = new Random();

    public static synchronized String generateRoomId() {
        if (generatedRoomIds.size() == 1_000_000) {
            throw new IllegalStateException("더 이상 새로운 방 ID를 생성할 수 없습니다.");
        }

        String roomId;
        do {
            roomId = String.format("%06d", random.nextInt(1_000_000)); // 000000 ~ 999999
        } while (generatedRoomIds.contains(roomId));

        generatedRoomIds.add(roomId);
        return roomId;
    }
}