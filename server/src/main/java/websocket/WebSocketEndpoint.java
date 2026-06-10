package websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketEndpoint {

    private final WebSocketHandler handler;

    public WebSocketEndpoint(WebSocketHandler handler) {
        this.handler = handler;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        handler.onMessage(session, message);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed");
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
    }
}
