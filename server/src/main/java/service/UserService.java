package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.result.AuthResult;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthResult register(UserData user) throws Exception {

        if (user == null ||
                user.username() == null ||
                user.password() == null ||
                user.email() == null) {

            throw new Exception("Error: bad request");
        }

        if (dataAccess.getUser(user.username()) != null) {
            throw new Exception("Error: already taken");
        }

        String hashedPassword =
                BCrypt.hashpw(user.password(), BCrypt.gensalt());

        UserData hashedUser =
                new UserData(user.username(), hashedPassword, user.email());

        dataAccess.createUser(hashedUser);

        String authToken = UUID.randomUUID().toString();

        dataAccess.createAuth(
                new AuthData(authToken, user.username())
        );

        return new AuthResult(user.username(), authToken);
    }

    public AuthResult login(UserData user) throws Exception {

        if (user == null ||
                user.username() == null ||
                user.password() == null) {

            throw new Exception("Error: bad request");
        }

        UserData storedUser =
                dataAccess.getUser(user.username());

        if (storedUser == null) {
            throw new Exception("Error: unauthorized");
        }

        if (!BCrypt.checkpw(user.password(), storedUser.password())) {
            throw new Exception("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();

        dataAccess.createAuth(
                new AuthData(authToken, user.username())
        );

        return new AuthResult(user.username(), authToken);
    }

    public void logout(String authToken) throws Exception {

        if (authToken == null) {
            throw new Exception("Error: unauthorized");
        }

        if (dataAccess.getAuth(authToken) == null) {
            throw new Exception("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }
}