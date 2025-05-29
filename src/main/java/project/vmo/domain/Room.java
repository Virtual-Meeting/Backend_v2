package project.vmo.domain;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.vmo.util.RoomIdGenerator;
import project.vmo.Service.SendService;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public class Room implements Closeable {
    private final Logger log = LoggerFactory.getLogger(Room.class);

    private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();

    @Getter
    private final MediaPipeline pipeline;

    @Getter
    private final String roomId;

    @Getter
    private String leaderSessionId;

    @Getter
    private String leaderName;

    public Room(MediaPipeline pipeline, String leaderSessionId, String leaderName) {
        this.roomId = RoomIdGenerator.generateRoomId();
        this.pipeline = pipeline;
        this.leaderSessionId = leaderSessionId;
        this.leaderName = leaderName;
        log.info("{} 방이 생성되었습니다.", this.roomId);
    }

    public Collection<UserSession> getParticipants() {
        return participants.values();
    }

    public UserSession getRandomParticipant() {
        List<String> participantKeys = new ArrayList<>(participants.keySet());
        String keyForRandomParticipant = participantKeys.get(ThreadLocalRandom.current().nextInt(participants.size()));
        return participants.get(keyForRandomParticipant);
    }

    public void addParticipant(UserSession participant) {
        participants.put(participant.getSession().getId(), participant);
    }

    public void removeParticipant(String sessionId) {
        if (!participants.containsKey(sessionId)) {
            throw new IllegalArgumentException("해당 ID를 가진 사용자는 참가자 리스트에 없으므로 삭제할 수 없습니다.");
        }

        try (UserSession user = participants.remove(sessionId)) {} catch (Exception e) {
            log.warn("참가자 삭제 후 사용자 제거 중 오류가 발생했습니다.", e);
        }
    }

    public void changeRoomLeader(String roomLeaderId, String username) {
        this.leaderSessionId = roomLeaderId;
        this.leaderName = username;
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    @Override
    public void close() {
        for (final UserSession user : participants.values()) {
            try {
                user.close();
                final JsonObject participantLeftJson = new JsonObject();
                participantLeftJson.addProperty("action", "exitRoom");
                SendService.sendMessage(user.getSession(), participantLeftJson);
            } catch (IOException e) {
                log.debug("{} 방 - {} / {} 사용자에게 퇴장 알림 메시지를 보내는 데 실패했습니다.", this.roomId, user.getUsername(), user.getSession().getId(), e);
            }
        }

        participants.clear();

        pipeline.release(new Continuation<Void>() {

            @Override
            public void onSuccess(Void result) throws Exception {
                log.trace("{} 방 - MediaPipeline 해제 완료", Room.this.roomId);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("{} 방 - MediaPipeline 해제 시 오류 발생", Room.this.roomId, cause);
            }
        });

        log.debug("{} 방이 정상적으로 닫혔습니다.", this.roomId);
    }
}