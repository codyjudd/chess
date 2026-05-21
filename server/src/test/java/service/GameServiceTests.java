package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Test;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.AuthResult;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private AuthResult login(UserService users) throws Exception {
        return users.register(new UserData("a", "b", "c"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        MemoryDataAccess data = new MemoryDataAccess();
        AuthResult auth = login(new UserService(data));
        CreateGameResult result = new GameService(data).createGame(auth.authToken(), new CreateGameRequest("game"));
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameUnauthorized() {
        GameService service = new GameService(new MemoryDataAccess());
        assertThrows(ServiceException.class, () -> service.createGame("bad", new CreateGameRequest("game")));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        MemoryDataAccess data = new MemoryDataAccess();
        AuthResult auth = login(new UserService(data));
        GameService service = new GameService(data);
        service.createGame(auth.authToken(), new CreateGameRequest("game"));
        ListGamesResult result = service.listGames(auth.authToken());
        assertEquals(1, result.games().size());
    }

    @Test
    public void listGamesUnauthorized() {
        GameService service = new GameService(new MemoryDataAccess());
        assertThrows(ServiceException.class, () -> service.listGames("bad"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        MemoryDataAccess data = new MemoryDataAccess();
        AuthResult auth = login(new UserService(data));
        GameService service = new GameService(data);
        CreateGameResult game = service.createGame(auth.authToken(), new CreateGameRequest("game"));
        assertDoesNotThrow(() -> service.joinGame(auth.authToken(), new JoinGameRequest("WHITE", game.gameID())));
    }

    @Test
    public void joinGameAlreadyTaken() throws Exception {
        MemoryDataAccess data = new MemoryDataAccess();
        UserService users = new UserService(data);
        AuthResult auth1 = users.register(new UserData("a", "b", "c"));
        AuthResult auth2 = users.register(new UserData("x", "y", "z"));
        GameService service = new GameService(data);
        CreateGameResult game = service.createGame(auth1.authToken(), new CreateGameRequest("game"));
        service.joinGame(auth1.authToken(), new JoinGameRequest("WHITE", game.gameID()));
        assertThrows(ServiceException.class, () -> service.joinGame(auth2.authToken(), new JoinGameRequest("WHITE", game.gameID())));
    }
}
