package client;

import ui.ChessClient;
import ui.PostloginRepl;
import ui.PreloginRepl;

public class ClientMain {

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument; using 8080.");
            }
        }

        ChessClient client = new ChessClient(port);

        while (true) {
            PreloginRepl prelogin = new PreloginRepl(client);
            boolean loggedIn = prelogin.run();

            if (!loggedIn) {
                break;
            }

            PostloginRepl postlogin = new PostloginRepl(client);
            postlogin.run();
        }
    }
}