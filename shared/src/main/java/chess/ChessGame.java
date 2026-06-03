package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public enum TeamColor {
        WHITE, BLACK
    }

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : piece.pieceMoves(board, startPosition)) {
            if (moveKeepsKingSafe(move, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> moves = validMoves(move.getStartPosition());

        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException();
        }

        movePiece(move, piece);
        switchTurn();
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);

        if (kingPosition == null) {
            return false;
        }

        return isPositionAttacked(kingPosition, oppositeTeam(teamColor));
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoLegalMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoLegalMoves(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private boolean moveKeepsKingSafe(ChessMove move, TeamColor teamColor) {
        ChessBoard originalBoard = board;
        board = copyBoard(originalBoard);

        ChessPiece piece = board.getPiece(move.getStartPosition());
        movePiece(move, piece);

        boolean safe = !isInCheck(teamColor);
        board = originalBoard;

        return safe;
    }

    private void movePiece(ChessMove move, ChessPiece piece) {
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        board.addPiece(move.getEndPosition(), piece);
    }

    private boolean hasNoLegalMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (pieceHasLegalMove(row, col, teamColor)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean pieceHasLegalMove(int row, int col, TeamColor teamColor) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getTeamColor() != teamColor) {
            return false;
        }

        Collection<ChessMove> moves = validMoves(position);
        return moves != null && !moves.isEmpty();
    }

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                if (isTeamKing(position, teamColor)) {
                    return position;
                }
            }
        }

        return null;
    }

    private boolean isTeamKing(ChessPosition position, TeamColor teamColor) {
        ChessPiece piece = board.getPiece(position);

        return piece != null
                && piece.getTeamColor() == teamColor
                && piece.getPieceType() == ChessPiece.PieceType.KING;
    }

    private boolean isPositionAttacked(ChessPosition position, TeamColor attackingTeam) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                if (pieceAttacksPosition(row, col, position, attackingTeam)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean pieceAttacksPosition(int row, int col, ChessPosition target, TeamColor attackingTeam) {
        ChessPosition start = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(start);

        if (piece == null || piece.getTeamColor() != attackingTeam) {
            return false;
        }

        for (ChessMove move : piece.pieceMoves(board, start)) {
            if (move.getEndPosition().equals(target)) {
                return true;
            }
        }

        return false;
    }

    private ChessBoard copyBoard(ChessBoard originalBoard) {
        ChessBoard copy = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                copy.addPiece(position, originalBoard.getPiece(position));
            }
        }

        return copy;
    }

    private TeamColor oppositeTeam(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {
            return TeamColor.BLACK;
        }

        return TeamColor.WHITE;
    }

    private void switchTurn() {
        teamTurn = oppositeTeam(teamTurn);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }

        return teamTurn == chessGame.teamTurn
                && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}