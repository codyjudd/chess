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
import service.request.LoginRequest;

public class Server {
    private final Javalin javalin;
    private final Gson gson = new Gson();

    private final DataAccess dataAccess = new MemoryDataAccess();
    private final ClearService clearService = new ClearService(dataAccess);
    private final UserService userService = new UserService(dataAccess);
    private final GameService gameService = new GameService(dataAccess);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                sendEmpty(ctx);
            } catch (Exception ex) {
                sendError(ctx, 500, ex);
            }
        });

        javalin.post("/user", ctx -> {
            try {
                UserData request = gson.fromJson(ctx.body(), UserData.class);
                sendJson(ctx, userService.register(request));
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });

        javalin.post("/session", ctx -> {
            try {
                LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
                sendJson(ctx, userService.login(request));
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                userService.logout(authToken);
                sendEmpty(ctx);
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                sendJson(ctx, gameService.listGames(authToken));
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });

        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                sendJson(ctx, gameService.createGame(authToken, request));
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });

        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
                gameService.joinGame(authToken, request);
                sendEmpty(ctx);
            } catch (Exception ex) {
                sendError(ctx, getStatus(ex.getMessage()), ex);
            }
        });
    }

    private void sendEmpty(io.javalin.http.Context ctx) {
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result("{}");
    }

    private void sendJson(io.javalin.http.Context ctx, Object result) {
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }

    private void sendError(io.javalin.http.Context ctx, int status, Exception ex) {
        String message = ex.getMessage();

        if (message == null || message.isEmpty()) {
            message = "Error: server error";
        }

        if (!message.startsWith("Error:")) {
            message = "Error: " + message;
        }

        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result("{\"message\":\"" + message + "\"}");
    }

    private int getStatus(String message) {
        if (message == null) {
            return 500;
        }

        String lower = message.toLowerCase();

        if (lower.contains("bad request")) {
            return 400;
        }

        if (lower.contains("unauthorized")) {
            return 401;
        }

        if (lower.contains("already taken")) {
            return 403;
        }

        return 500;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}