package server;

import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        // DELETE /db
    }

    public AuthData register(String username,
                             String password,
                             String email)
            throws ResponseException {
        // POST /user
        return null;
    }

    public AuthData login(String username,
                          String password)
            throws ResponseException {
        // POST /session
        return null;
    }

    public void logout(String authToken)
            throws ResponseException {
        // DELETE /session
    }

    public int createGame(String authToken,
                          String gameName)
            throws ResponseException {
        // POST /game
        return 0;
    }

    public Collection<GameData> listGames(String authToken)
            throws ResponseException {
        // GET /game
        return null;
    }

    public void joinGame(String authToken,
                         String playerColor,
                         int gameID)
            throws ResponseException {
        // PUT /game
    }

    private <T> T makeRequest(String method,
                              String path,
                              Object request,
                              Class<T> responseClass,
                              String authToken)
            throws ResponseException {
        return null;
    }
}