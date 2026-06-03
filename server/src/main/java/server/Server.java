package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import io.javalin.Javalin;
import io.javalin.http.Context;
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

    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        DataAccess dataAccess = createDataAccess();

        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        registerRoutes();
    }

    private DataAccess createDataAccess() {
        try {
            return new MySqlDataAccess();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void registerRoutes() {
        javalin.delete("/db", this::clearDatabase);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.post("/game", this::createGame);
        javalin.get("/game", this::listGames);
        javalin.put("/game", this::joinGame);
    }

    private void clearDatabase(Context ctx) {
        try {
            clearService.clear();
            sendEmptySuccess(ctx);
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void register(Context ctx) {
        try {
            UserData request = gson.fromJson(ctx.body(), UserData.class);
            sendJson(ctx, userService.register(request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void login(Context ctx) {
        try {
            UserData request = gson.fromJson(ctx.body(), UserData.class);
            sendJson(ctx, userService.login(request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void logout(Context ctx) {
        try {
            userService.logout(ctx.header("authorization"));
            sendEmptySuccess(ctx);
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void createGame(Context ctx) {
        try {
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            sendJson(ctx, gameService.createGame(ctx.header("authorization"), request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void listGames(Context ctx) {
        try {
            sendJson(ctx, gameService.listGames(ctx.header("authorization")));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void joinGame(Context ctx) {
        try {
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(ctx.header("authorization"), request);
            sendEmptySuccess(ctx);
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void sendJson(Context ctx, Object result) {
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    private void sendEmptySuccess(Context ctx) {
        ctx.status(200);
        ctx.result("{}");
    }

    private void handleException(Context ctx, Exception ex) {
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