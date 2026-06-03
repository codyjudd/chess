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
    private final Gson gson = new Gson();
    private final DataAccess dataAccess;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final Javalin javalin;

    public Server() {
        dataAccess = createDataAccess();
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
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.post("/game", this::createGame);
        javalin.get("/game", this::listGames);
        javalin.put("/game", this::joinGame);
    }

    private void clear(Context ctx) {
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
            sendSuccess(ctx, userService.register(request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void login(Context ctx) {
        try {
            UserData request = gson.fromJson(ctx.body(), UserData.class);
            sendSuccess(ctx, userService.login(request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void logout(Context ctx) {
        try {
            userService.logout(getAuthToken(ctx));
            sendEmptySuccess(ctx);
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void createGame(Context ctx) {
        try {
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
            sendSuccess(ctx, gameService.createGame(getAuthToken(ctx), request));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void listGames(Context ctx) {
        try {
            sendSuccess(ctx, gameService.listGames(getAuthToken(ctx)));
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private void joinGame(Context ctx) {
        try {
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(getAuthToken(ctx), request);
            sendEmptySuccess(ctx);
        } catch (Exception ex) {
            handleException(ctx, ex);
        }
    }

    private String getAuthToken(Context ctx) {
        return ctx.header("authorization");
    }

    private void sendSuccess(Context ctx, Object result) {
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    private void sendEmptySuccess(Context ctx) {
        ctx.status(200);
        ctx.result("{}");
    }

    private void handleException(Context ctx, Exception ex) {
        String message = ex.getMessage();
        ctx.status(statusCode(message));
        ctx.result(gson.toJson(new ErrorResult(message)));
    }

    private int statusCode(String message) {
        if ("Error: bad request".equals(message)) {
            return 400;
        }
        if ("Error: unauthorized".equals(message)) {
            return 401;
        }
        if ("Error: already taken".equals(message)) {
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