package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var request = new RegisterRequest(username, password, email);
        return makeRequest("POST", "/user", request, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var request = new LoginRequest(username, password);
        return makeRequest("POST", "/session", request, AuthData.class, null);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var request = new CreateGameRequest(gameName);
        var result = makeRequest("POST", "/game", request, CreateGameResult.class, authToken);
        return result.gameID();
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        var result = makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
        return result.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var request = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", request, null, authToken);
    }

    private <T> T makeRequest(String method, String path, Object requestBody,
                              Class<T> responseClass, String authToken) throws ResponseException {
        try {
            var url = URI.create(serverUrl + path).toURL();
            var http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod(method);
            http.setRequestProperty("Content-Type", "application/json");

            if (authToken != null) {
                http.setRequestProperty("authorization", authToken);
            }

            if (requestBody != null) {
                http.setDoOutput(true);
                try (OutputStream output = http.getOutputStream()) {
                    output.write(gson.toJson(requestBody).getBytes());
                }
            }

            int status = http.getResponseCode();

            if (status >= 400) {
                throw new ResponseException("Request failed");
            }

            if (responseClass == null) {
                return null;
            }

            try (var input = new InputStreamReader(http.getInputStream())) {
                return gson.fromJson(input, responseClass);
            }

        } catch (Exception e) {
            if (e instanceof ResponseException) {
                throw (ResponseException) e;
            }
            throw new ResponseException("Unable to connect to server");
        }
    }

    private record RegisterRequest(String username, String password, String email) {}
    private record LoginRequest(String username, String password) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(String playerColor, int gameID) {}

    private record CreateGameResult(int gameID) {}
    private record ListGamesResult(Collection<GameData> games) {}
}