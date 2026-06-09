package websocket;

public class WebSocketFacade {
    private final ServerMessageObserver observer;

    public WebSocketFacade(ServerMessageObserver observer) {
        this.observer = observer;
    }

    public void send(String message) {
        // TODO  send the message over WebSocket
    }
}
