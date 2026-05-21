package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import service.result.AuthResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthResult register(UserData user) throws Exception {
        if (user == null || user.username() == null || user.password() == null || user.email() == null) {
            throw new Exception("Error: bad request");
        }

        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Error: already taken");
        }

        dataAccess.createUser(user);

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, user.username()));

        return new AuthResult(user.username(), token);
    }

    public AuthResult login(UserData user) throws Exception {
        if (user == null || user.username() == null || user.password() == null) {
            throw new Exception("Error: bad request");
        }

        UserData existingUser = dataAccess.getUser(user.username());

        if (existingUser == null || !existingUser.password().equals(user.password())) {
            throw new Exception("Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, user.username()));

        return new AuthResult(user.username(), token);
    }

    public void logout(String authToken) throws Exception {
        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new Exception("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }
}