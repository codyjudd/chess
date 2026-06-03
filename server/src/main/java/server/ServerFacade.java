package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;

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

    public void clear() throws ResponseException {
        try {
            var url = URI.create(serverUrl + "/db").toURL();

            HttpURLConnection http =
                    (HttpURLConnection) url.openConnection();

            http.setRequestMethod("DELETE");
            http.connect();

            if (http.getResponseCode() != 200) {
                throw new ResponseException("Unable to clear database");
            }

        } catch (Exception e) {
            throw new ResponseException("Connection error");
        }
    }

    public AuthData register(String username,
                             String password,
                             String email)
            throws ResponseException {

        try {
            var url = URI.create(serverUrl + "/user").toURL();

            HttpURLConnection http =
                    (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            var request = new RegisterRequest(username, password, email);
            String json = gson.toJson(request);

            try (OutputStream output = http.getOutputStream()) {
                output.write(json.getBytes());
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

    public AuthData login(String username,
                          String password)
            throws ResponseException {

        try {
            var url = URI.create(serverUrl + "/session").toURL();

            HttpURLConnection http =
                    (HttpURLConnection) url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            var request = new LoginRequest(username, password);
            String json = gson.toJson(request);

            try (OutputStream output = http.getOutputStream()) {
                output.write(json.getBytes());
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

    public void logout(String authToken)
            throws ResponseException {

        try {
            var url = URI.create(serverUrl + "/session").toURL();

            HttpURLConnection http =
                    (HttpURLConnection) url.openConnection();

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

    private record RegisterRequest(
            String username,
            String password,
            String email) {
    }

    private record LoginRequest(
            String username,
            String password) {
    }
}