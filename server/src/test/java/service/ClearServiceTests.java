package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClearServiceTests {
    @Test
    public void clearSuccess() throws Exception {
        MemoryDataAccess dao = new MemoryDataAccess();
        dao.createUser(new UserData("cody", "pass", "cody@test.com"));
        new ClearService(dao).clear();
        Assertions.assertNull(dao.getUser("cody"));
    }
}
