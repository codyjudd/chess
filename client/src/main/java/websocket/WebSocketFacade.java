package websocket;

import com.google.gson.Gson;

public class WebSocketFacade {

    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketFacade(ServerMessageObserver observer) {
        this.observer = observer;
    }

    public void connect(int gameID) {
        // TODO
    }

    public void makeMove() {
        // TODO
    }

    public void leave() {
        // TODO
    }

    public void resign() {
        // TODO
    }

    private void send(String json) {
        // TODO
    }
}
