package service.result;

public record ListGameEntry(
        Integer gameID,
        String gameName,
        String whiteUsername,
        String blackUsername
) {
}
