package websocket;

public class WebSocketHandler {
    private final ConnectionManager connectionManager = new ConnectionManager();

    public void handleMessage(String message) {
        // TODO parsethe  UserGameCommand JSON
        // TODO  handle CONNECT, MAKE_MOVE, LEAVE, RESIGN
    }
}
