package websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

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

        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        );

        send(gson.toJson(command));
    }

    public void makeMove(ChessMove move) {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        send(gson.toJson(command));
    }

    public void leave() {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                authToken,
                gameID
        );

        send(gson.toJson(command));
    }

    public void resign() {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken,
                gameID
        );

        send(gson.toJson(command));
    }

    public void receive(String message) {
        observer.notify(message);
    }

    private void send(String json) {
        System.out.println("TODO send websocket message: " + json);
    }
}
