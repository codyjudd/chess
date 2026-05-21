package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.AuthResult;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

public class GameServiceTests {
    private AuthResult register(UserService userService) throws Exception {
        return userService.register(new UserData("cody", "pass", "cody@test.com"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        MemoryDataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        AuthResult auth = register(userService);
        CreateGameResult result = gameService.createGame(auth.authToken(), new CreateGameRequest("game"));
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameUnauthorized() {
        GameService gameService = new GameService(new MemoryDataAccess());
        Exception ex = Assertions.assertThrows(Exception.class,
                () -> gameService.createGame("bad", new CreateGameRequest("game")));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    public void listGamesSuccess() throws Exception {
        MemoryDataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        AuthResult auth = register(userService);
        gameService.createGame(auth.authToken(), new CreateGameRequest("game"));
        ListGamesResult result = gameService.listGames(auth.authToken());
        Assertions.assertEquals(1, result.games().size());
    }

    @Test
    public void listGamesUnauthorized() {
        GameService gameService = new GameService(new MemoryDataAccess());
        Exception ex = Assertions.assertThrows(Exception.class, () -> gameService.listGames("bad"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    public void joinGameSuccess() throws Exception {
        MemoryDataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        AuthResult auth = register(userService);
        CreateGameResult game = gameService.createGame(auth.authToken(), new CreateGameRequest("game"));
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth.authToken(), new JoinGameRequest("WHITE", game.gameID())));
    }

    @Test
    public void joinGameAlreadyTaken() throws Exception {
        MemoryDataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        GameService gameService = new GameService(dao);
        AuthResult auth = register(userService);
        CreateGameResult game = gameService.createGame(auth.authToken(), new CreateGameRequest("game"));
        gameService.joinGame(auth.authToken(), new JoinGameRequest("WHITE", game.gameID()));
        Exception ex = Assertions.assertThrows(Exception.class,
                () -> gameService.joinGame(auth.authToken(), new JoinGameRequest("WHITE", game.gameID())));
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }
}
