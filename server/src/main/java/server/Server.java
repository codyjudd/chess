package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import io.javalin.Javalin;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.UserService;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.ErrorResult;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();

    private final DataAccess dataAccess = new MemoryDataAccess();

    private final ClearService clearService = new ClearService(dataAccess);
    private final UserService userService = new UserService(dataAccess);
    private final GameService gameService = new GameService(dataAccess);

    public Server() {

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        // CLEAR
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result("{}");

            } catch (Exception ex) {

                ctx.status(500);
                ctx.json(new ErrorResult("Error: " + ex.getMessage()));
            }
        });

        // REGISTER
        javalin.post("/user", ctx -> {
            try {

                UserData request =
                        gson.fromJson(ctx.body(), UserData.class);

                var result = userService.register(request);

                ctx.status(200);
                ctx.json(result);

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: bad request")) {
                    ctx.status(400);
                }
                else if (msg.equals("Error: already taken")) {
                    ctx.status(403);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });

        // LOGIN
        javalin.post("/session", ctx -> {
            try {

                UserData request =
                        gson.fromJson(ctx.body(), UserData.class);

                var result = userService.login(request);

                ctx.status(200);
                ctx.json(result);

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: bad request")) {
                    ctx.status(400);
                }
                else if (msg.equals("Error: unauthorized")) {
                    ctx.status(401);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });

        // LOGOUT
        javalin.delete("/session", ctx -> {
            try {

                String auth =
                        ctx.header("authorization");

                userService.logout(auth);

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result("{}");

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: unauthorized")) {
                    ctx.status(401);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });

        // LIST GAMES
        javalin.get("/game", ctx -> {
            try {

                String auth =
                        ctx.header("authorization");

                var result =
                        gameService.listGames(auth);

                ctx.status(200);
                ctx.json(result);

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: unauthorized")) {
                    ctx.status(401);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });

        // CREATE GAME
        javalin.post("/game", ctx -> {
            try {

                String auth =
                        ctx.header("authorization");

                CreateGameRequest request =
                        gson.fromJson(ctx.body(), CreateGameRequest.class);

                var result =
                        gameService.createGame(auth, request);

                ctx.status(200);
                ctx.json(result);

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: bad request")) {
                    ctx.status(400);
                }
                else if (msg.equals("Error: unauthorized")) {
                    ctx.status(401);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });

        // JOIN GAME
        javalin.put("/game", ctx -> {
            try {

                String auth =
                        ctx.header("authorization");

                JoinGameRequest request =
                        gson.fromJson(ctx.body(), JoinGameRequest.class);

                gameService.joinGame(auth, request);

                ctx.status(200);
                ctx.contentType("application/json");
                ctx.result("{}");

            } catch (Exception ex) {

                String msg = ex.getMessage();

                if (msg.equals("Error: bad request")) {
                    ctx.status(400);
                }
                else if (msg.equals("Error: unauthorized")) {
                    ctx.status(401);
                }
                else if (msg.equals("Error: already taken")) {
                    ctx.status(403);
                }
                else {
                    ctx.status(500);
                }

                ctx.json(new ErrorResult(msg));
            }
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}