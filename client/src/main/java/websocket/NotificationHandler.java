package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import ui.BoardDrawer;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class NotificationHandler implements ServerMessageObserver {

    private final Gson gson = new Gson();

    private volatile ChessGame currentGame = null;
    private volatile ChessGame.TeamColor playerColor = null;

    public void setPlayerColor(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    public ChessGame getCurrentGame() {
        return currentGame;
    }

    @Override
    public void notify(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME    -> handleLoadGame(message);
            case ERROR        -> handleError(message);
            case NOTIFICATION -> handleNotification(message);
        }
    }

    private void handleLoadGame(String message) {
        LoadGameMessage loadMsg = gson.fromJson(message, LoadGameMessage.class);
        GameData gameData = loadMsg.getGame();

        if (gameData == null || gameData.game() == null) {
            System.out.println("\n[Error] Received invalid game data.");
            return;
        }

        currentGame = gameData.game();
        System.out.println();
        drawCurrentBoard();
        System.out.print("[game] >>> ");
    }

    private void handleError(String message) {
        ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
        System.out.println("\n" + error.getErrorMessage());
        System.out.print("[game] >>> ");
    }

    private void handleNotification(String message) {
        NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
        System.out.println("\n" + notification.getMessage());
        System.out.print("[game] >>> ");
    }

    public void drawCurrentBoard() {
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }

        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardDrawer.drawBlack(currentGame.getBoard());
        } else {
            BoardDrawer.drawWhite(currentGame.getBoard());
        }
    }
}
