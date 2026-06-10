package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public void onMessage(String message) {

        UserGameCommand command =
                gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {

            case CONNECT -> connect(command);

            case MAKE_MOVE -> makeMove(command);

            case LEAVE -> leave(command);

            case RESIGN -> resign(command);
        }
    }

    private void connect(UserGameCommand command) {
        // TODO
    }

    private void makeMove(UserGameCommand command) {
        // TODO
    }

    private void leave(UserGameCommand command) {
        // TODO
    }

    private void resign(UserGameCommand command) {
        // TODO
    }
}
