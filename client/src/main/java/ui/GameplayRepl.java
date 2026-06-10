package ui;

import chess.ChessMove;
import chess.ChessPosition;
import websocket.WebSocketFacade;

import java.util.Scanner;

public class GameplayRepl {

    private final Scanner scanner = new Scanner(System.in);
    private final WebSocketFacade webSocketFacade;

    public GameplayRepl(WebSocketFacade webSocketFacade) {
        this.webSocketFacade = webSocketFacade;
    }

    public void run() {
        System.out.println("Gameplay Started");
        help();

        while (true) {
            System.out.print("[game] >>> ");
            String line = scanner.nextLine().trim();

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help" -> help();
                case "redraw" -> redraw();
                case "move" -> makeMove(parts);
                case "highlight" -> highlight(parts);
                case "resign" -> resign();
                case "leave" -> {
                    leave();
                    return;
                }
                default -> System.out.println("Unknown command. Type help.");
            }
        }
    }

    private void help() {
        System.out.println("help - show commands");
        System.out.println("redraw - redraw chess board");
        System.out.println("move <start> <end> - example: move e2 e4");
        System.out.println("highlight <square> - example: highlight e2");
        System.out.println("resign - resign from the game");
        System.out.println("leave - leave the game");
    }

    private void redraw() {
        System.out.println("TODO: redraw board");
    }

    private void makeMove(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: move <start> <end>");
            return;
        }

        ChessPosition start = parsePosition(parts[1]);
        ChessPosition end = parsePosition(parts[2]);

        if (start == null || end == null) {
            System.out.println("Invalid square. Use format like e2 or e4.");
            return;
        }

        ChessMove move = new ChessMove(start, end, null);
        webSocketFacade.makeMove(move);
    }

    private void highlight(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: highlight <square>");
            return;
        }

        ChessPosition position = parsePosition(parts[1]);

        if (position == null) {
            System.out.println("Invalid square. Use format like e2.");
            return;
        }

        System.out.println("TODO: highlight moves for " + parts[1]);
    }

    private ChessPosition parsePosition(String input) {
        if (input == null || input.length() != 2) {
            return null;
        }

        char colChar = Character.toLowerCase(input.charAt(0));
        char rowChar = input.charAt(1);

        if (colChar < 'a' || colChar > 'h') {
            return null;
        }

        if (rowChar < '1' || rowChar > '8') {
            return null;
        }

        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;

        return new ChessPosition(row, col);
    }

    private void resign() {
        System.out.print("Are you sure you want to resign? yes/no: ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (answer.equals("yes")) {
            webSocketFacade.resign();
            System.out.println("Resign command sent.");
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void leave() {
        webSocketFacade.leave();
        System.out.println("Leaving game.");
    }
}
