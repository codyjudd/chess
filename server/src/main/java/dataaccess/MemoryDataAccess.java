package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameID = 1;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (auths.remove(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
    }

    @Override
    public int createGame(String gameName) {
        int gameID = nextGameID;
        nextGameID++;
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("bad request");
        }
        games.put(game.gameID(), game);
    }
}
