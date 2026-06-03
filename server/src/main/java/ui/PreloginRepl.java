package ui;

import java.util.Scanner;


public class PreloginRepl {

    private final ChessClient client;
    private final Scanner scanner;

    public PreloginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE + EscapeSequences.SET_TEXT_BOLD
                + "♕  Welcome to 240 Chess  ♕" + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println("Type 'help' for a list of commands.");
        // will do escape sequences later
        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN + "[chess] "
                    + EscapeSequences.RESET_TEXT_COLOR + ">>> ");
            String line = scanner.nextLine().trim();

            if (line.isBlank()) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            String cmd = tokens[0].toLowerCase();
            String[] args = java.util.Arrays.copyOfRange(tokens, 1, tokens.length);

            switch (cmd) {
                case "help" -> printHelp();
                case "quit", "exit" -> {
                    System.out.println("Goodbye!");
                    return false;
                }
                case "register" -> {
                    String result = client.register(args);
                    System.out.println(result);
                    if (client.isLoggedIn()) {
                        return true;
                    }
                }
                case "login" -> {
                    String result = client.login(args);
                    System.out.println(result);
                    if (client.isLoggedIn()) {
                        return true;
                    }
                }
                default -> System.out.println("Unknown command: '" + cmd + "'. Type 'help' for options.");
            }
        }
    }

    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW);
        System.out.println("  register <username> <password> <email>  - Create a new account");
        System.out.println("  login    <username> <password>          - Sign in to your account");
        System.out.println("  quit                                    - Exit the program");
        System.out.println("  help                                    - Show this message");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }
}

