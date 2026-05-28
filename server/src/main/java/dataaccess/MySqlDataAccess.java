package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess {

    private final Gson gson = new Gson();

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (Connection conn = DatabaseManager.getConnection()) {

            String[] statements = {

                    """
                    CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        PRIMARY KEY (username)
                    )
                    """,

                    """
                    CREATE TABLE IF NOT EXISTS auth (
                        authToken VARCHAR(255) NOT NULL,
                        username VARCHAR(255) NOT NULL,
                        PRIMARY KEY (authToken)
                    )
                    """,

                    """
                    CREATE TABLE IF NOT EXISTS game (
                        gameID INT NOT NULL AUTO_INCREMENT,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        gameName VARCHAR(255) NOT NULL,
                        game TEXT NOT NULL,
                        PRIMARY KEY (gameID)
                    )
                    """
            };

            for (String statement : statements) {
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database");
        }
    }

    @Override
    public void clear() throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM auth")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM game")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM user")) {
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear database");
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, user.username());
                ps.setString(2, user.password());
                ps.setString(3, user.email());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create user");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "SELECT * FROM user WHERE username=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to get user");
        }

        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create auth");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "SELECT * FROM auth WHERE authToken=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, authToken);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("authToken"),
                                rs.getString("username")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to get auth");
        }

        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "DELETE FROM auth WHERE authToken=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, authToken);

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete auth");
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {

        ChessGame game = new ChessGame();

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "INSERT INTO game (gameName, game) VALUES (?, ?)";

            try (PreparedStatement ps =
                         conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, gameName);
                ps.setString(2, gson.toJson(game));

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game");
        }

        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "SELECT * FROM game WHERE gameID=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, gameID);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        ChessGame game =
                                gson.fromJson(rs.getString("game"), ChessGame.class);

                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                game
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to get game");
        }

        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {

        ArrayList<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = "SELECT * FROM game";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        ChessGame game =
                                gson.fromJson(rs.getString("game"), ChessGame.class);

                        games.add(new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                game
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games");
        }

        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

        try (Connection conn = DatabaseManager.getConnection()) {

            String sql = """
                    UPDATE game
                    SET whiteUsername=?,
                        blackUsername=?,
                        gameName=?,
                        game=?
                    WHERE gameID=?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, game.gameName());
                ps.setString(4, gson.toJson(game.game()));
                ps.setInt(5, game.gameID());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game");
        }
    }
}