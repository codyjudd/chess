package ui;

import chess.*;
import websocket.NotificationHandler;
import websocket.WebSocketFacade;

import java.util.Collection;
import java.util.Scanner;

public class GameplayRepl {

    private final Scanner scanner = new Scanner(System.in);
    private final WebSocketFacade webSocketFacade;
    private final NotificationHandler handler;
    private final ChessGame.TeamColor playerColor;

    public GameplayRepl(WebSocketFacade webSocketFacade, NotificationHandler handler,
                        ChessGame.TeamColor playerColor) {
        this.webSocketFacade = webSocketFacade;
        this.handler = handler;
        this.playerColor = playerColor;
    }

    public void run() {
        System.out.println("Gameplay started. Type 'help' for commands.");

        while (true) {
            System.out.print("[game] >>> ");
            String line;
            try {
                line = scanner.nextLine().trim();
            } catch (Exception e) {
                break;
            }

            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help"      -> help();
                case "redraw"    -> redraw();
                case "move"      -> makeMove(parts);
                case "highlight" -> highlight(parts);
                case "resign"    -> resign(line);
                case "leave"     -> { leave(); return; }
                default          -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void help() {
        System.out.println("  help                           - show this help text");
        System.out.println("  redraw                         - redraw the chess board");
        System.out.println("  move <start> <end> [promo]     - make a move, e.g. 'move e2 e4'");
        System.out.println("                                   optional promotion piece: q r b n");
        System.out.println("  highlight <square>             - show legal moves, e.g. 'highlight e2'");
        System.out.println("  resign                         - forfeit the game");
        System.out.println("  leave                          - leave the game (returns to lobby)");
    }

    private void redraw() {
        handler.drawCurrentBoard();
    }

    private void makeMove(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Usage: move <start> <end> [promotion: q r b n]");
            return;
        }

        ChessPosition start = parsePosition(parts[1]);
        ChessPosition end   = parsePosition(parts[2]);

        if (start == null || end == null) {
            System.out.println("Invalid square. Use format like e2 or e4.");
            return;
        }

        ChessPiece.PieceType promotion = null;
        if (parts.length >= 4) {
            promotion = parsePromotion(parts[3]);
            if (promotion == null) {
                System.out.println("Invalid promotion piece. Use q, r, b, or n.");
                return;
            }
        }

        webSocketFacade.makeMove(new ChessMove(start, end, promotion));
    }

    private void highlight(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: highlight <square>");
            return;
        }

        ChessPosition pos = parsePosition(parts[1]);
        if (pos == null) {
            System.out.println("Invalid square. Use format like e2.");
            return;
        }

        ChessGame game = handler.getCurrentGame();
        if (game == null) {
            System.out.println("No game loaded.");
            return;
        }

        Collection<ChessMove> moves = game.validMoves(pos);

        if (playerColor == ChessGame.TeamColor.BLACK) {
            BoardDrawer.drawBlackHighlight(game.getBoard(), pos, moves);
        } else {
            BoardDrawer.drawWhiteHighlight(game.getBoard(), pos, moves);
        }
    }

    private void resign(String fullLine) {
        // If they typed "resign yes" skip the prompt
        String[] parts = fullLine.split("\\s+");
        String confirmation;
        if (parts.length >= 2 && parts[1].equalsIgnoreCase("yes")) {
            confirmation = "yes";
        } else {
            System.out.print("Are you sure you want to resign? (yes/no): ");
            try {
                confirmation = scanner.nextLine().trim().toLowerCase();
            } catch (Exception e) {
                return;
            }
        }

        if (confirmation.equals("yes")) {
            webSocketFacade.resign();
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void leave() {
        webSocketFacade.leave();
        System.out.println("Leaving game.");
        webSocketFacade.close();
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
        return new ChessPosition(rowChar - '1' + 1, colChar - 'a' + 1);
    }

    private ChessPiece.PieceType parsePromotion(String s) {
        return switch (s.toLowerCase()) {
            case "q" -> ChessPiece.PieceType.QUEEN;
            case "r" -> ChessPiece.PieceType.ROOK;
            case "b" -> ChessPiece.PieceType.BISHOP;
            case "n" -> ChessPiece.PieceType.KNIGHT;
            default  -> null;
        };
    }
}
