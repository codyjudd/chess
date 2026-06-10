package websocket;

public class ConsoleObserver implements ServerMessageObserver {

    @Override
    public void notify(String message) {
        System.out.println();
        System.out.println("[SERVER] " + message);
    }
}
