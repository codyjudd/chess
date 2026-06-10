package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectionManager {

    private final Map<Integer, Set<Session>> connections = new HashMap<>();

    public void add(int gameID, Session session) {
        connections.putIfAbsent(gameID, new HashSet<>());
        connections.get(gameID).add(session);
    }

    public void remove(int gameID, Session session) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(session);
        }
    }

    public void broadcast(int gameID, String message) throws Exception {
        // TODO
    }
}
