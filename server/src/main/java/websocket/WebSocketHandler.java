package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.HashSet;
import java.util.Set;

public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();
    private final Set<Integer> completedGames = new HashSet<>();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case MAKE_MOVE -> makeMove(session, message);
                case LEAVE -> leave(session, command);
                case RESIGN -> resign(command);
            }
        } catch (Exception ex) {
            sendError(session, ex.getMessage());
        }
    }

    public void onClose(Session session) {
        connections.removeFromAll(session);
    }

    private void connect(Session session, UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData game = requireGame(command.getGameID());

        connections.add(game.gameID(), session);
        connections.send(session, gson.toJson(new LoadGameMessage(game)));

        connections.broadcastExcept(
                game.gameID(),
                session,
                gson.toJson(new NotificationMessage(connectMessage(auth.username(), game)))
        );
    }

    private void makeMove(Session session, String rawMessage) throws Exception {
        MakeMoveCommand command = gson.fromJson(rawMessage, MakeMoveCommand.class);

        AuthData auth = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());

        if (completedGames.contains(gameData.gameID())) {
            throw new Exception("Error: game is over");
        }

        ChessGame game = gameData.game();
        ChessGame.TeamColor playerColor = playerColor(auth.username(), gameData);

        if (playerColor == null) {
            throw new Exception("Error: observer cannot move");
        }

        if (playerColor != game.getTeamTurn()) {
            throw new Exception("Error: not your turn");
        }

        ChessMove move = command.getMove();

        try {
            game.makeMove(move);
        } catch (InvalidMoveException ex) {
            throw new Exception("Error: invalid move");
        }

        GameData updatedGame = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );

        dataAccess.updateGame(updatedGame);

        connections.broadcast(gameData.gameID(), gson.toJson(new LoadGameMessage(updatedGame)));

        connections.broadcastExcept(
                gameData.gameID(),
                session,
                gson.toJson(new NotificationMessage(auth.username() + " moved " + move + "."))
        );

        sendGameStatusNotifications(gameData.gameID(), game);
    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData game = requireGame(command.getGameID());

        GameData updatedGame = removePlayerIfNeeded(auth.username(), game);
        dataAccess.updateGame(updatedGame);

        connections.remove(game.gameID(), session);

        connections.broadcast(
                game.gameID(),
                gson.toJson(new NotificationMessage(auth.username() + " left the game."))
        );
    }

    private void resign(UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData game = requireGame(command.getGameID());

        if (completedGames.contains(game.gameID())) {
            throw new Exception("Error: game is already over");
        }

        if (playerColor(auth.username(), game) == null) {
            throw new Exception("Error: observer cannot resign");
        }

        completedGames.add(game.gameID());

        connections.broadcast(
                game.gameID(),
                gson.toJson(new NotificationMessage(auth.username() + " resigned."))
        );
    }

    private void sendGameStatusNotifications(int gameID, ChessGame game) throws Exception {
        ChessGame.TeamColor nextTurn = game.getTeamTurn();

        if (game.isInCheckmate(nextTurn)) {
            completedGames.add(gameID);
            connections.broadcast(gameID, gson.toJson(new NotificationMessage(nextTurn + " is in checkmate.")));
        } else if (game.isInStalemate(nextTurn)) {
            completedGames.add(gameID);
            connections.broadcast(gameID, gson.toJson(new NotificationMessage(nextTurn + " is in stalemate.")));
        } else if (game.isInCheck(nextTurn)) {
            connections.broadcast(gameID, gson.toJson(new NotificationMessage(nextTurn + " is in check.")));
        }
    }

    private AuthData requireAuth(String authToken) throws Exception {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        return auth;
    }

    private GameData requireGame(Integer gameID) throws Exception {
        if (gameID == null) {
            throw new Exception("Error: bad request");
        }

        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new Exception("Error: bad request");
        }

        return game;
    }

    private String connectMessage(String username, GameData game) {
        ChessGame.TeamColor color = playerColor(username, game);

        if (color == ChessGame.TeamColor.WHITE) {
            return username + " connected as white.";
        }

        if (color == ChessGame.TeamColor.BLACK) {
            return username + " connected as black.";
        }

        return username + " connected as an observer.";
    }

    private ChessGame.TeamColor playerColor(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }

        if (username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }

        return null;
    }

    private GameData removePlayerIfNeeded(String username, GameData game) {
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (username.equals(white)) {
            white = null;
        }

        if (username.equals(black)) {
            black = null;
        }

        return new GameData(game.gameID(), white, black, game.gameName(), game.game());
    }

    private void sendError(Session session, String message) {
        try {
            String safeMessage = message;

            if (safeMessage == null || !safeMessage.toLowerCase().contains("error")) {
                safeMessage = "Error: " + safeMessage;
            }

            connections.send(session, gson.toJson(new ErrorMessage(safeMessage)));
        } catch (Exception ignored) {
        }
    }
}
