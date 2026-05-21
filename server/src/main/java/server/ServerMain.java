package server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
        System.out.println("Chess server running on port 8080");
    }
}
