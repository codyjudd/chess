package ui;

import java.util.Scanner;

public class GameplayRepl {

    private final Scanner scanner = new Scanner(System.in);

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
        // TODO: call BoardDrawer
    }

    private void makeMove(String[] parts) {
        // TODO: parse move and send MAKE_MOVE websocket command
    }

    private void highlight(String[] parts) {
        // TODO: highlight legal moves locally
    }

    private void resign() {
        // TODO: confirm and send RESIGN websocket command
    }

    private void leave() {
        // TODO: send LEAVE websocket command
    }
}
