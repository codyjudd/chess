package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess {

    private final Gson gson = new Gson();

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {

        try {
            String[] createStatements = {
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
                    game TEXT,
                    PRIMARY KEY (gameID)
                )
                """
            };

            for (String statement : createStatements) {
                try (var conn = DatabaseManager.getConnection();
                     var preparedStatement = conn.prepareStatement(statement)) {

                    preparedStatement.executeUpdate();
                }
            }

        } catch (Exception ex) {
            throw new DataAccessException("Unable to configure database");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String[] statements = {
                "DELETE FROM auth",
                "DELETE FROM game",
                "DELETE FROM user"
        };

        try {
            for (String statement : statements) {
                try (var conn = DatabaseManager.getConnection();
                     var preparedStatement = conn.prepareStatement(statement)) {

                    preparedStatement.executeUpdate();
                }
            }

        } catch (Exception ex) {
            throw new DataAccessException("Unable to clear database");
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, user.password());
            preparedStatement.setString(3, user.email());

            preparedStatement.executeUpdate();

        } catch (Exception ex) {
            throw new DataAccessException("Unable to create user");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT username, password, email FROM user WHERE username = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, username);

            try (var resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new UserData(
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email")
                    );
                }
            }

            return null;

        } catch (Exception ex) {
            throw new DataAccessException("Unable to get user");
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, auth.authToken());
            preparedStatement.setString(2, auth.username());

            preparedStatement.executeUpdate();

        } catch (Exception ex) {
            throw new DataAccessException("Unable to create auth");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String statement = "SELECT authToken, username FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, authToken);

            try (var resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new AuthData(
                            resultSet.getString("authToken"),
                            resultSet.getString("username")
                    );
                }
            }

            return null;

        } catch (Exception ex) {
            throw new DataAccessException("Unable to get auth");
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auth WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, authToken);

            preparedStatement.executeUpdate();

        } catch (Exception ex) {
            throw new DataAccessException("Unable to delete auth");
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String statement = "INSERT INTO game (gameName, game) VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            ChessGame chessGame = new ChessGame();

            preparedStatement.setString(1, gameName);
            preparedStatement.setString(2, gson.toJson(chessGame));

            preparedStatement.executeUpdate();

            try (var keys = preparedStatement.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new DataAccessException("Unable to create game");

        } catch (Exception ex) {
            throw new DataAccessException("Unable to create game");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        String statement = """
                SELECT gameID, whiteUsername, blackUsername, gameName, game
                FROM game
                WHERE gameID = ?
                """;

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setInt(1, gameID);

            try (var resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    ChessGame chessGame = gson.fromJson(
                            resultSet.getString("game"),
                            ChessGame.class
                    );

                    return new GameData(
                            resultSet.getInt("gameID"),
                            resultSet.getString("whiteUsername"),
                            resultSet.getString("blackUsername"),
                            resultSet.getString("gameName"),
                            chessGame
                    );
                }
            }

            return null;

        } catch (Exception ex) {
            throw new DataAccessException("Unable to get game");
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {

        Collection<GameData> games = new ArrayList<>();

        String statement = """
                SELECT gameID, whiteUsername, blackUsername, gameName, game
                FROM game
                """;

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement);
             var resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {

                ChessGame chessGame = gson.fromJson(
                        resultSet.getString("game"),
                        ChessGame.class
                );

                games.add(new GameData(
                        resultSet.getInt("gameID"),
                        resultSet.getString("whiteUsername"),
                        resultSet.getString("blackUsername"),
                        resultSet.getString("gameName"),
                        chessGame
                ));
            }

            return games;

        } catch (Exception ex) {
            throw new DataAccessException("Unable to list games");
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

        String statement = """
                UPDATE game
                SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ?
                WHERE gameID = ?
                """;

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {

            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, gson.toJson(game.game()));
            preparedStatement.setInt(5, game.gameID());

            preparedStatement.executeUpdate();

        } catch (Exception ex) {
            throw new DataAccessException("Unable to update game");
        }
    }
}