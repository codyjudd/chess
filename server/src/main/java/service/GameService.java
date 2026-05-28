package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResult createGame(
            String authToken,
            CreateGameRequest request) throws Exception {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        if (request == null || request.gameName() == null) {
            throw new Exception("Error: bad request");
        }

        int gameID =
                dataAccess.createGame(request.gameName());

        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(String authToken)
            throws Exception {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        return new ListGamesResult(
                dataAccess.listGames()
        );
    }

    public void joinGame(
            String authToken,
            JoinGameRequest request) throws Exception {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        if (request == null ||
                request.playerColor() == null) {

            throw new Exception("Error: bad request");
        }

        GameData game =
                dataAccess.getGame(request.gameID());

        if (game == null) {
            throw new Exception("Error: bad request");
        }

        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (request.playerColor().equalsIgnoreCase("WHITE")) {

            if (whiteUsername != null) {
                throw new Exception("Error: already taken");
            }

            whiteUsername = auth.username();

        } else if (request.playerColor().equalsIgnoreCase("BLACK")) {

            if (blackUsername != null) {
                throw new Exception("Error: already taken");
            }

            blackUsername = auth.username();

        } else {
            throw new Exception("Error: bad request");
        }

        GameData updatedGame =
                new GameData(
                        game.gameID(),
                        whiteUsername,
                        blackUsername,
                        game.gameName(),
                        game.game()
                );

        dataAccess.updateGame(updatedGame);
    }
}