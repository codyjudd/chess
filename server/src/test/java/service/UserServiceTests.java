package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Test;
import service.request.LoginRequest;
import service.result.AuthResult;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    @Test
    public void registerSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        AuthResult result = service.register(new UserData("a", "b", "c"));
        assertEquals("a", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerAlreadyTaken() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("a", "b", "c"));
        assertThrows(ServiceException.class, () -> service.register(new UserData("a", "b", "c")));
    }

    @Test
    public void loginSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("a", "b", "c"));
        AuthResult result = service.login(new LoginRequest("a", "b"));
        assertEquals("a", result.username());
    }

    @Test
    public void loginBadPassword() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("a", "b", "c"));
        assertThrows(ServiceException.class, () -> service.login(new LoginRequest("a", "wrong")));
    }

    @Test
    public void logoutSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        AuthResult result = service.register(new UserData("a", "b", "c"));
        assertDoesNotThrow(() -> service.logout(result.authToken()));
    }

    @Test
    public void logoutBadToken() {
        UserService service = new UserService(new MemoryDataAccess());
        assertThrows(ServiceException.class, () -> service.logout("bad"));
    }
}
