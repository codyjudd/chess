package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
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
}