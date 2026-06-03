package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

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
        try {
            var url = URI.create(serverUrl + "/db").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("DELETE");
            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Unable to clear database");
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public AuthData register(String username, String password, String email)
            throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/user").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            var request = new RegisterRequest(username, password, email);

            try (OutputStream output = http.getOutputStream()) {
                output.write(gson.toJson(request).getBytes());
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Register failed");
            }

            try (InputStreamReader reader =
                         new InputStreamReader(http.getInputStream())) {
                return gson.fromJson(reader, AuthData.class);
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public AuthData login(String username, String password)
            throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/session").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            var request = new LoginRequest(username, password);

            try (OutputStream output = http.getOutputStream()) {
                output.write(gson.toJson(request).getBytes());
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Login failed");
            }

            try (InputStreamReader reader =
                         new InputStreamReader(http.getInputStream())) {
                return gson.fromJson(reader, AuthData.class);
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public void logout(String authToken) throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/session").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("DELETE");
            http.addRequestProperty("authorization", authToken);

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Logout failed");
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public int createGame(String authToken, String gameName)
            throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/game").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            http.addRequestProperty("authorization", authToken);

            var request = new CreateGameRequest(gameName);

            try (OutputStream output = http.getOutputStream()) {
                output.write(gson.toJson(request).getBytes());
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Create game failed");
            }

            try (InputStreamReader reader =
                         new InputStreamReader(http.getInputStream())) {
                CreateGameResult result =
                        gson.fromJson(reader, CreateGameResult.class);

                return result.gameID();
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public Collection<GameData> listGames(String authToken)
            throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/game").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("GET");
            http.addRequestProperty("authorization", authToken);

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("List games failed");
            }

            try (InputStreamReader reader =
                         new InputStreamReader(http.getInputStream())) {
                ListGamesResult result =
                        gson.fromJson(reader, ListGamesResult.class);

                return result.games();
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public void joinGame(String authToken, String playerColor, int gameID)
            throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/game").toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            http.setRequestMethod("PUT");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            http.addRequestProperty("authorization", authToken);

            var request = new JoinGameRequest(playerColor, gameID);

            try (OutputStream output = http.getOutputStream()) {
                output.write(gson.toJson(request).getBytes());
            }

            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Join game failed");
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    private record RegisterRequest(String username, String password, String email) {
    }

    private record LoginRequest(String username, String password) {
    }
}