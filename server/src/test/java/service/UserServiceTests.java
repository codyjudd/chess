package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.result.AuthResult;

public class UserServiceTests {
    @Test
    public void registerSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        AuthResult result = service.register(new UserData("cody", "pass", "cody@test.com"));
        Assertions.assertEquals("cody", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void registerAlreadyTaken() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("cody", "pass", "cody@test.com"));
        Exception ex = Assertions.assertThrows(Exception.class,
                () -> service.register(new UserData("cody", "pass", "cody@test.com")));
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    public void loginSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("cody", "pass", "cody@test.com"));
        AuthResult result = service.login(new UserData("cody", "pass", null));
        Assertions.assertEquals("cody", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void loginBadPassword() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        service.register(new UserData("cody", "pass", "cody@test.com"));
        Exception ex = Assertions.assertThrows(Exception.class,
                () -> service.login(new UserData("cody", "wrong", null)));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    public void logoutSuccess() throws Exception {
        UserService service = new UserService(new MemoryDataAccess());
        AuthResult result = service.register(new UserData("cody", "pass", "cody@test.com"));
        Assertions.assertDoesNotThrow(() -> service.logout(result.authToken()));
    }

    @Test
    public void logoutBadToken() {
        UserService service = new UserService(new MemoryDataAccess());
        Exception ex = Assertions.assertThrows(Exception.class, () -> service.logout("bad"));
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }
}
