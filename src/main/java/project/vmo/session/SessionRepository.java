package project.vmo.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import project.vmo.domain.UserSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRepository {
    private static final Logger log = LoggerFactory.getLogger(SessionRepository.class);

    private final ConcurrentHashMap<String, UserSession> userSessionBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession userSession) {
        userSessionBySessionId.put(userSession.getSession().getId(), userSession);
    }

    public UserSession getBySession(WebSocketSession session) {
        return userSessionBySessionId.get(session.getId());
    }

    public UserSession getBySessionId(String sessionId) {
        return userSessionBySessionId.get(sessionId);
    }

    public void removeBySession(WebSocketSession session) {
        UserSession userSession = userSessionBySessionId.remove(session.getId());

        if (userSession != null) {
            try {
                userSession.close();
            } catch (IOException e) {
                log.warn("UserSession 자원 해제 중 오류 발생: {}", e.getMessage());
            }
        } else {
            log.warn("세션에 해당하는 UserSession이 존재하지 않아 제거할 수 없습니다. sessionId: {}", session.getId());
        }
    }
}