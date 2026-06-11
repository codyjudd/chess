package ui;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import server.ServerFacade;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChessClient {

    private final ServerFacade facade;
    private final int port;
    private AuthData currentUser = null;
    private List<GameData> lastGameList = new ArrayList<>();

    public ChessClient(int port) {
        this.port = port;
        this.facade = new ServerFacade(port);
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String currentUsername() {
        return currentUser != null ? currentUser.username() : null;
    }

    public String register(String[] args) {
        if (args.length != 3) return "Usage: register <username> <password> <email>";
        try {
            currentUser = facade.register(args[0], args[1], args[2]);
            return "Registered and logged in as " + currentUser.username() + ".";
        } catch (ResponseException e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    public String login(String[] args) {
        if (args.length != 2) return "Usage: login <username> <password>";
        try {
            currentUser = facade.login(args[0], args[1]);
            return "Logged in as " + currentUser.username() + ".";
        } catch (ResponseException e) {
            return "Login failed: " + e.getMessage();
        }
    }

    public String logout() {
        try {
            facade.logout(currentUser.authToken());
            String name = currentUser.username();
            currentUser = null;
            lastGameList.clear();
            return "Logged out " + name + ".";
        } catch (ResponseException e) {
            return "Logout failed: " + e.getMessage();
        }
    }

    public String createGame(String[] args) {
        if (args.length < 1) return "Usage: create <game name>";
        String gameName = String.join(" ", args);
        try {
            facade.createGame(currentUser.authToken(), gameName);
            return "Created game \"" + gameName + "\".";
        } catch (ResponseException e) {
            return "Could not create game: " + e.getMessage();
        }
    }

    public String listGames() {
        try {
            Collection<GameData> games = facade.listGames(currentUser.authToken());
            lastGameList = new ArrayList<>(games);

            if (lastGameList.isEmpty()) {
                return "No games found. Create one with: create <name>";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lastGameList.size(); i++) {
                GameData g = lastGameList.get(i);
                String white = g.whiteUsername() != null ? g.whiteUsername() : "(open)";
                String black = g.blackUsername() != null ? g.blackUsername() : "(open)";
                sb.append(String.format("  %d. %-20s  White: %-12s  Black: %s%n",
                        i + 1, g.gameName(), white, black));
            }
            return sb.toString().stripTrailing();
        } catch (ResponseException e) {
            return "Could not list games: " + e.getMessage();
        }
    }

    public String playGame(String[] args) {
        if (args.length != 2) return "Usage: play <game number> <WHITE|BLACK>";

        int index = parseGameIndex(args[0]);
        if (index < 0) return "Invalid game number. Run 'list' to see available games.";

        String colorArg = args[1].toUpperCase();
        if (!colorArg.equals("WHITE") && !colorArg.equals("BLACK")) {
            return "Color must be WHITE or BLACK.";
        }

        GameData game = lastGameList.get(index);
        try {
            facade.joinGame(currentUser.authToken(), colorArg, game.gameID());
            System.out.println("Joined \"" + game.gameName() + "\" as " + colorArg + ".");
        } catch (ResponseException e) {
            return "Could not join game: " + e.getMessage();
        }

        ChessGame.TeamColor color = colorArg.equals("WHITE")
                ? ChessGame.TeamColor.WHITE
                : ChessGame.TeamColor.BLACK;

        startGameplay(game.gameID(), color);
        return "";
    }

    public String observeGame(String[] args) {
        if (args.length != 1) return "Usage: observe <game number>";

        int index = parseGameIndex(args[0]);
        if (index < 0) return "Invalid game number. Run 'list' to see available games.";

        GameData game = lastGameList.get(index);
        System.out.println("Observing \"" + game.gameName() + "\".");

        startGameplay(game.gameID(), null);
        return "";
    }

    private void startGameplay(int gameID, ChessGame.TeamColor playerColor) {
        try {
            String serverUrl = "http://localhost:" + port;
            NotificationHandler handler = new NotificationHandler();
            handler.setPlayerColor(playerColor);

            WebSocketFacade ws = new WebSocketFacade(serverUrl, handler);
            ws.connect(currentUser.authToken(), gameID);

            GameplayRepl gameplay = new GameplayRepl(ws, handler, playerColor);
            gameplay.run();
        } catch (Exception e) {
            System.out.println("Error connecting to game: " + e.getMessage());
        }
    }

    private int parseGameIndex(String s) {
        try {
            int n = Integer.parseInt(s);
            if (n < 1 || n > lastGameList.size()) return -1;
            return n - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
