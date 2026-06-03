package client;

import exception.ResponseException;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.util.Collection;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerSuccess() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void registerFailDuplicateUser() throws ResponseException {
        facade.register("cody", "password", "cody@email.com");
        Assertions.assertThrows(ResponseException.class, () ->
                facade.register("cody", "password", "cody2@email.com"));
    }

    @Test
    public void loginSuccess() throws ResponseException {
        facade.register("cody", "password", "cody@email.com");
        AuthData auth = facade.login("cody", "password");
        Assertions.assertNotNull(auth.authToken());
    }

    @Test
    public void loginFailWrongPassword() throws ResponseException {
        facade.register("cody", "password", "cody@email.com");
        Assertions.assertThrows(ResponseException.class, () ->
                facade.login("cody", "wrong"));
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutFailBadAuth() {
        Assertions.assertThrows(ResponseException.class, () ->
                facade.logout("bad-token"));
    }

    @Test
    public void createGameSuccess() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        int gameID = facade.createGame(auth.authToken(), "my game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    public void createGameFailBadAuth() {
        Assertions.assertThrows(ResponseException.class, () ->
                facade.createGame("bad-token", "my game"));
    }

    @Test
    public void listGamesSuccess() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        facade.createGame(auth.authToken(), "my game");
        Collection<?> games = facade.listGames(auth.authToken());
        Assertions.assertFalse(games.isEmpty());
    }

    @Test
    public void listGamesFailBadAuth() {
        Assertions.assertThrows(ResponseException.class, () ->
                facade.listGames("bad-token"));
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        int gameID = facade.createGame(auth.authToken(), "my game");
        Assertions.assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), "WHITE", gameID));
    }

    @Test
    public void joinGameFailBadGameID() throws ResponseException {
        AuthData auth = facade.register("cody", "password", "cody@email.com");
        Assertions.assertThrows(ResponseException.class, () ->
                facade.joinGame(auth.authToken(), "WHITE", 999999));
    }

    @Test
    public void clearSuccess() {
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }
}
