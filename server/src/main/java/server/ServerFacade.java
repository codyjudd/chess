package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import service.request.CreateGameRequest;
import service.result.CreateGameResult;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
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
}