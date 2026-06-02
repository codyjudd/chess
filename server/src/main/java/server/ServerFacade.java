package server;

import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username,
                             String password,
                             String email)
            throws ResponseException {
        return null;
    }

    public AuthData login(String username,
                          String password)
            throws ResponseException {
        return null;
    }

    public void logout(String authToken)
            throws ResponseException {
    }

    public int createGame(String authToken,
                          String gameName)
            throws ResponseException {
        return 0;
    }

    public Collection<GameData> listGames(String authToken)
            throws ResponseException {
        return null;
    }

    public void joinGame(String authToken,
                         String playerColor,
                         int gameID)
            throws ResponseException {
    }

    public void clear()
            throws ResponseException {
    }
}