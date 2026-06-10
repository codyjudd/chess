package ui;

import java.util.Arrays;
import java.util.Scanner;


public class PostloginRepl {

    private final ChessClient client;
    private final Scanner scanner;

    public PostloginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }
    //will do escape sequences later
    public void run() {
        System.out.println("Logged in as " + EscapeSequences.SET_TEXT_BOLD
                + client.currentUsername() + EscapeSequences.RESET_TEXT_BOLD_FAINT
                + ". Type 'help' for commands.");

        while (client.isLoggedIn()) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + "["
                    + client.currentUsername() + "] "
                    + EscapeSequences.RESET_TEXT_COLOR + ">>> ");
            String line = scanner.nextLine().trim();

            if (line.isBlank()) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toLowerCase();
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            String result = switch (cmd) {
                case "help"    -> helpText();
                case "logout"  -> client.logout();
                case "create"  -> client.createGame(args);
                case "list"    -> client.listGames();
                case "play"    -> client.playGame(args);
                case "observe" -> client.observeGame(args);
                default        -> "Unknown command: '" + cmd + "'. Type 'help' for options.";
            };

            if (!result.isBlank()) {
                System.out.println(result);
            }
        }
    }

    private String helpText() {
        return EscapeSequences.SET_TEXT_COLOR_YELLOW
                + "  create <name>                    - Create a new game\n"
                + "  list                             - List all available games\n"
                + "  play   <game number> <WHITE|BLACK> - Join a game as a player\n"
                + "  observe <game number>            - Watch a game\n"
                + "  logout                           - Sign out\n"
                + "  help                             - Show this message"
                + EscapeSequences.RESET_TEXT_COLOR;
    }
}
