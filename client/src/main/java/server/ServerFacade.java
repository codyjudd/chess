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
        serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        return makeRequest("POST", "/user", null,
                new RegisterRequest(username, password, email), AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        return makeRequest("POST", "/session", null,
                new LoginRequest(username, password), AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", authToken, null, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        CreateGameResult result = makeRequest("POST", "/game", authToken,
                new CreateGameRequest(gameName), CreateGameResult.class);
        return result.gameID();
    }

    public Collection<GameData> listGames(String authToken) throws ResponseException {
        ListGamesResult result = makeRequest("GET", "/game", authToken, null, ListGamesResult.class);
        return result.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        makeRequest("PUT", "/game", authToken,
                new JoinGameRequest(playerColor, gameID), null);
    }

    private <T> T makeRequest(String method, String path, String authToken,
                              Object body, Class<T> responseClass) throws ResponseException {
        try {
            HttpURLConnection http = createConnection(method, path, authToken);

            if (body != null) {
                writeBody(http, body);
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Request failed");
            }

            if (responseClass == null) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(http.getInputStream())) {
                return gson.fromJson(reader, responseClass);
            }

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    private HttpURLConnection createConnection(String method, String path, String authToken) throws Exception {
        var url = URI.create(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();

        http.setRequestMethod(method);
        http.addRequestProperty("Content-Type", "application/json");

        if (authToken != null) {
            http.addRequestProperty("authorization", authToken);
        }

        if (method.equals("POST") || method.equals("PUT")) {
            http.setDoOutput(true);
        }

        return http;
    }

    private void writeBody(HttpURLConnection http, Object body) throws Exception {
        try (OutputStream output = http.getOutputStream()) {
            output.write(gson.toJson(body).getBytes());
        }
    }

    private record RegisterRequest(String username, String password, String email) {}
    private record LoginRequest(String username, String password) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record CreateGameResult(int gameID) {}
    private record ListGamesResult(Collection<GameData> games) {}
}