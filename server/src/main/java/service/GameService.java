package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
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

    public ListGamesResult listGames(String authToken) throws ServiceException {
        checkAuth(authToken);
        try {
            return new ListGamesResult(dataAccess.listGames());
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws ServiceException {
        checkAuth(authToken);
        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new ServiceException(400, "bad request");
        }
        try {
            int id = dataAccess.createGame(request.gameName());
            return new CreateGameResult(id);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public void joinGame(String authToken, JoinGameRequest request) throws ServiceException {
        AuthData auth = checkAuth(authToken);
        if (request == null || request.gameID() == null || request.playerColor() == null) {
            throw new ServiceException(400, "bad request");
        }
        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(request.playerColor());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(400, "bad request");
        }
        try {
            GameData game = dataAccess.getGame(request.gameID());
            if (game == null) {
                throw new ServiceException(400, "bad request");
            }
            if (color == ChessGame.TeamColor.WHITE) {
                if (game.whiteUsername() != null) {
                    throw new ServiceException(403, "already taken");
                }
                dataAccess.updateGame(new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game()));
            } else {
                if (game.blackUsername() != null) {
                    throw new ServiceException(403, "already taken");
                }
                dataAccess.updateGame(new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    private AuthData checkAuth(String authToken) throws ServiceException {
        if (authToken == null || authToken.isBlank()) {
            throw new ServiceException(401, "unauthorized");
        }
        try {
            AuthData auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException(401, "unauthorized");
            }
            return auth;
        } catch (ServiceException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }
}
