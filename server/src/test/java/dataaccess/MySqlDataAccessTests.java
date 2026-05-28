package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {

    private MySqlDataAccess dao;

    @BeforeEach
    public void setup() throws Exception {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    public void clearSuccess() throws Exception {
        dao.createUser(new UserData("cody", "password", "cody@email.com"));
        dao.createAuth(new AuthData("token123", "cody"));
        dao.createGame("test game");
        dao.clear();
        assertNull(dao.getUser("cody"));
        assertNull(dao.getAuth("token123"));
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    public void createUserSuccess() throws Exception {
        dao.createUser(new UserData("cody", "password", "cody@email.com"));
        UserData found = dao.getUser("cody");
        assertNotNull(found);
        assertEquals("cody", found.username());
        assertEquals("cody@email.com", found.email());
    }

    @Test
    public void createUserDuplicateFails() throws Exception {
        UserData user = new UserData("cody", "password", "cody@email.com");
        dao.createUser(user);
        assertThrows(DataAccessException.class, () -> dao.createUser(user));
    }

    @Test
    public void getUserSuccess() throws Exception {
        dao.createUser(new UserData("cody", "password", "cody@email.com"));
        UserData found = dao.getUser("cody");
        assertNotNull(found);
        assertEquals("cody", found.username());
    }

    @Test
    public void getUserNotFound() throws Exception {
        assertNull(dao.getUser("missing"));
    }

    @Test
    public void createAuthSuccess() throws Exception {
        dao.createAuth(new AuthData("token123", "cody"));
        AuthData found = dao.getAuth("token123");
        assertNotNull(found);
        assertEquals("token123", found.authToken());
        assertEquals("cody", found.username());
    }

    @Test
    public void createAuthDuplicateFails() throws Exception {
        AuthData auth = new AuthData("token123", "cody");
        dao.createAuth(auth);
        assertThrows(DataAccessException.class, () -> dao.createAuth(auth));
    }

    @Test
    public void getAuthSuccess() throws Exception {
        dao.createAuth(new AuthData("token123", "cody"));
        AuthData found = dao.getAuth("token123");
        assertNotNull(found);
        assertEquals("cody", found.username());
    }

    @Test
    public void getAuthNotFound() throws Exception {
        assertNull(dao.getAuth("badtoken"));
    }

    @Test
    public void deleteAuthSuccess() throws Exception {
        dao.createAuth(new AuthData("token123", "cody"));
        dao.deleteAuth("token123");
        assertNull(dao.getAuth("token123"));
    }

    @Test
    public void deleteAuthMissingDoesNothing() throws Exception {
        dao.deleteAuth("missingtoken");
        assertNull(dao.getAuth("missingtoken"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        int gameID = dao.createGame("my game");
        GameData game = dao.getGame(gameID);
        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals("my game", game.gameName());
        assertNotNull(game.game());
    }

    @Test
    public void createGameNullNameFails() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    public void getGameSuccess() throws Exception {
        int gameID = dao.createGame("my game");
        GameData game = dao.getGame(gameID);
        assertNotNull(game);
        assertEquals("my game", game.gameName());
    }

    @Test
    public void getGameNotFound() throws Exception {
        assertNull(dao.getGame(999999));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        dao.createGame("game one");
        dao.createGame("game two");
        assertEquals(2, dao.listGames().size());
    }

    @Test
    public void listGamesEmpty() throws Exception {
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    public void updateGameSuccess() throws Exception {
        int gameID = dao.createGame("my game");
        GameData oldGame = dao.getGame(gameID);
        GameData updatedGame = new GameData(oldGame.gameID(), "cody", null, oldGame.gameName(), oldGame.game());
        dao.updateGame(updatedGame);
        GameData found = dao.getGame(gameID);
        assertEquals("cody", found.whiteUsername());
        assertNull(found.blackUsername());
    }

    @Test
    public void updateGameMissingDoesNothing() throws Exception {
        int beforeSize = dao.listGames().size();
        GameData fakeGame = new GameData(999999, "cody", null, "fake", new chess.ChessGame());
        dao.updateGame(fakeGame);
        assertEquals(beforeSize, dao.listGames().size());
        assertNull(dao.getGame(999999));
    }
}
