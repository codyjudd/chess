package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGameEntry;
import service.result.ListGamesResult;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws Exception {
        if (dataAccess.getAuth(authToken) == null) {
            throw new Exception("Error: unauthorized");
        }

        if (request == null || request.gameName() == null) {
            throw new Exception("Error: bad request");
        }

        int gameID = dataAccess.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        if (dataAccess.getAuth(authToken) == null) {
            throw new Exception("Error: unauthorized");
        }

        Collection<GameData> games = dataAccess.listGames();
        ArrayList<ListGameEntry> entries = new ArrayList<>();

        for (GameData game : games) {
            entries.add(new ListGameEntry(
                    game.gameID(),
                    game.gameName(),
                    game.whiteUsername(),
                    game.blackUsername()
            ));
        }

        return new ListGamesResult(entries);
    }

    public void joinGame(String authToken, JoinGameRequest request) throws Exception {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }

        if (request == null || request.playerColor() == null) {
            throw new Exception("Error: bad request");
        }

        GameData game = dataAccess.getGame(request.gameID());

        if (game == null) {
            throw new Exception("Error: bad request");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (request.playerColor().equals("WHITE")) {
            if (white != null) {
                throw new Exception("Error: already taken");
            }
            white = auth.username();
        } else if (request.playerColor().equals("BLACK")) {
            if (black != null) {
                throw new Exception("Error: already taken");
            }
            black = auth.username();
        } else {
            throw new Exception("Error: bad request");
        }

        GameData updated = new GameData(
                game.gameID(),
                white,
                black,
                game.gameName(),
                game.game()
        );

        dataAccess.updateGame(updated);
    }
}
