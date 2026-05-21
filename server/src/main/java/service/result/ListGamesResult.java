package service.result;

import java.util.Collection;

public record ListGamesResult(Collection<ListGameEntry> games) {
}
