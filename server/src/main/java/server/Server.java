package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
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

    private final DataAccess dataAccess;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        try {
            dataAccess = new MySqlDataAccess();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        registerRoutes();
    }

    private void registerRoutes() {
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.post("/user", ctx -> {
            try {
                UserData request = gson.fromJson(ctx.body(), UserData.class);
                var result = userService.register(request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.post("/session", ctx -> {
            try {
                UserData request = gson.fromJson(ctx.body(), UserData.class);
                var result = userService.login(request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                userService.logout(authToken);
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                var result = gameService.createGame(authToken, request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                var result = gameService.listGames(authToken);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });

        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
                gameService.joinGame(authToken, request);
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception ex) {
                handleException(ctx, ex);
            }
        });
    }

    private void handleException(io.javalin.http.Context ctx, Exception ex) {
        String message = ex.getMessage();
        if ("Error: bad request".equals(message)) {
            ctx.status(400);
        } else if ("Error: unauthorized".equals(message)) {
            ctx.status(401);
        } else if ("Error: already taken".equals(message)) {
            ctx.status(403);
        } else {
            ctx.status(500);
        }
        ctx.result(gson.toJson(new ErrorResult(message)));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
