package project.vmo.permission;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionRepository {
    private final Map<String, String> permittedUsers = new HashMap<>();

    public void addPermittedUser(String sessionId, String roomId) {
        permittedUsers.put(sessionId, roomId);
    }

    public void removePermittedUser(String sessionId) {
        permittedUsers.remove(sessionId);
    }

    public void removeAllPermittedUsers(String roomId) {
        permittedUsers.entrySet().removeIf(entry -> roomId.equals(entry.getValue()));
    }

    public boolean checkPermittedUser(String sessionId) {
        return permittedUsers.containsKey(sessionId);
    }

    public long countPermittedUser(String roomId) {
        return permittedUsers.values().stream()
                .filter(id -> id.equals(roomId))
                .count();
    }
}
