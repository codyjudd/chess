package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.request.LoginRequest;
import service.result.AuthResult;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthResult register(UserData user) throws ServiceException {
        if (user == null || isBlank(user.username()) || isBlank(user.password()) || isBlank(user.email())) {
            throw new ServiceException(400, "bad request");
        }
        try {
            if (dataAccess.getUser(user.username()) != null) {
                throw new ServiceException(403, "already taken");
            }
            dataAccess.createUser(user);
            String token = UUID.randomUUID().toString();
            dataAccess.createAuth(new AuthData(token, user.username()));
            return new AuthResult(user.username(), token);
        } catch (ServiceException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public AuthResult login(LoginRequest request) throws ServiceException {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new ServiceException(400, "bad request");
        }
        try {
            UserData user = dataAccess.getUser(request.username());
            if (user == null || !user.password().equals(request.password())) {
                throw new ServiceException(401, "unauthorized");
            }
            String token = UUID.randomUUID().toString();
            dataAccess.createAuth(new AuthData(token, user.username()));
            return new AuthResult(user.username(), token);
        } catch (ServiceException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public void logout(String authToken) throws ServiceException {
        if (isBlank(authToken)) {
            throw new ServiceException(401, "unauthorized");
        }
        try {
            if (dataAccess.getAuth(authToken) == null) {
                throw new ServiceException(401, "unauthorized");
            }
            dataAccess.deleteAuth(authToken);
        } catch (ServiceException e) {
            throw e;
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
