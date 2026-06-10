package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case MAKE_MOVE -> makeMove(session, command);
                case LEAVE -> leave(session, command);
                case RESIGN -> resign(session, command);
            }

        } catch (Exception ex) {
            sendError(session, "could not process websocket command");
        }
    }

    private void connect(Session session, UserGameCommand command) throws Exception {
        connections.add(command.getGameID(), session);

        String notification = gson.toJson(
                new NotificationMessage("A user connected to game " + command.getGameID())
        );

        connections.broadcastExcept(command.getGameID(), session, notification);
    }

    private void makeMove(Session session, UserGameCommand command) throws Exception {
        String notification = gson.toJson(
                new NotificationMessage("A move was made in game " + command.getGameID())
        );

        connections.broadcastExcept(command.getGameID(), session, notification);
    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        connections.remove(command.getGameID(), session);

        String notification = gson.toJson(
                new NotificationMessage("A user left game " + command.getGameID())
        );

        connections.broadcastExcept(command.getGameID(), session, notification);
    }

    private void resign(Session session, UserGameCommand command) throws Exception {
        String notification = gson.toJson(
                new NotificationMessage("A user resigned from game " + command.getGameID())
        );

        connections.broadcast(command.getGameID(), notification);
    }

    private void sendError(Session session, String message) {
        try {
            connections.send(session, gson.toJson(new ErrorMessage(message)));
        } catch (Exception ignored) {
        }
    }
}
