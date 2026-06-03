package dataaccess;

public class DataAccessException extends Exception {
    public DataAccessException(String message) {
        super(message.startsWith("Error") ? message : "Error: " + message);
    }
}
