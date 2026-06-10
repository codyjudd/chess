package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;

public class WebSocketFacade {

    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    private String authToken;
    private Integer gameID;

    public WebSocketFacade(ServerMessageObserver observer) {
        this.observer = observer;
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

    public void receive(String message) {
        observer.notify(message);
    }

    private void send(String json) {
        System.out.println("TODO send websocket message: " + json);
    }
}
