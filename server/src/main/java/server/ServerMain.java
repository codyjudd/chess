package server;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        Server server = new Server();

        server.run(8080);

        System.out.println("Server started on port 8080");
    }
}