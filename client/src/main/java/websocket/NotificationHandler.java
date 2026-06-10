package websocket;

import com.google.gson.Gson;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class NotificationHandler implements ServerMessageObserver {

    private final Gson gson = new Gson();

    @Override
    public void notify(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> handleLoadGame(message);
            case ERROR -> handleError(message);
            case NOTIFICATION -> handleNotification(message);
        }
    }

    private void handleLoadGame(String message) {
        System.out.println();
        System.out.println("[LOAD GAME]");
        System.out.println(message);
    }

    private void handleError(String message) {
        ErrorMessage error = gson.fromJson(message, ErrorMessage.class);

        System.out.println();
        System.out.println(error.getErrorMessage());
    }

    private void handleNotification(String message) {
        NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);

        System.out.println();
        System.out.println(notification.getMessage());
    }
}
