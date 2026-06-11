package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;

import jakarta.websocket.*;
import java.net.URI;

@ClientEndpoint
public class WebSocketFacade {

    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    private Session session;
    private String authToken;
    private Integer gameID;

    public WebSocketFacade(String serverUrl, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(wsUrl));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        observer.notify(message);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.out.println("WebSocket error: " + t.getMessage());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
    }

    public void connect(String authToken, Integer gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
        send(gson.toJson(new ConnectCommand(authToken, gameID)));
    }

    public void makeMove(ChessMove move) {
        send(gson.toJson(new MakeMoveCommand(authToken, gameID, move)));
    }

    public void leave() {
        send(gson.toJson(new LeaveCommand(authToken, gameID)));
    }

    public void resign() {
        send(gson.toJson(new ResignCommand(authToken, gameID)));
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception ignored) {}
    }

    private void send(String json) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(json);
            } else {
                System.out.println("WebSocket not connected.");
            }
        } catch (Exception e) {
            System.out.println("Failed to send message: " + e.getMessage());
        }
    }
}
