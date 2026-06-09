package websocket;

public interface ServerMessageObserver {
    void notify(String message);
}
