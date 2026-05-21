package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    private ChessMove lastMove;

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
        lastMove = null;
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor teamTurn) {
        this.teamTurn = teamTurn;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
        lastMove = null;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            addEnPassant(possibleMoves, startPosition, piece);
        }

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            ChessBoard copy = copyBoard(board);
            makeMoveOnBoard(copy, move);

            if (!isInCheckOnBoard(copy, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public void makeMove(ChessMove move)
            throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null) {
            throw new InvalidMoveException();
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());

        if (legalMoves == null ||
                !legalMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        makeMoveOnBoard(board, move);
        lastMove = move;

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        }
        else {
            teamTurn = TeamColor.WHITE;
        }
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckOnBoard(board, teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {

        if (!isInCheck(teamColor)) {
            return false;
        }
        return teamHasNoValidMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {

        if (isInCheck(teamColor)) {
            return false;
        }
        return teamHasNoValidMoves(teamColor);
    }



    private boolean teamHasNoValidMoves(
            TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);

                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);

                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void addEnPassant(Collection<ChessMove> possibleMoves,
                              ChessPosition startPosition,
                              ChessPiece piece) {
        if (lastMove == null) {
            return;
        }

        ChessPiece lastPiece = board.getPiece(lastMove.getEndPosition());

        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        if (lastPiece.getTeamColor() == piece.getTeamColor()) {
            return;
        }

        int lastStartRow = lastMove.getStartPosition().getRow();
        int lastEndRow = lastMove.getEndPosition().getRow();

        if (Math.abs(lastStartRow - lastEndRow) != 2) {
            return;
        }

        int myRow = startPosition.getRow();
        int myCol = startPosition.getColumn();
        int enemyRow = lastMove.getEndPosition().getRow();
        int enemyCol = lastMove.getEndPosition().getColumn();

        if (myRow == enemyRow && Math.abs(myCol - enemyCol) == 1) {
            int direction = 1;

            if (piece.getTeamColor() == TeamColor.BLACK) {
                direction = -1;
            }

            ChessPosition endPosition = new ChessPosition(myRow + direction, enemyCol);
            possibleMoves.add(new ChessMove(startPosition, endPosition, null));
        }
    }

    private boolean isInCheckOnBoard( ChessBoard currentBoard, TeamColor teamColor) {
        ChessPosition kingPosition =
                findKing(currentBoard, teamColor);

        if (kingPosition == null) {
            return false;
        }

        TeamColor enemyColor;

        if (teamColor == TeamColor.WHITE) {
            enemyColor = TeamColor.BLACK;
        }

        else {
            enemyColor = TeamColor.WHITE;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position =
                        new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> moves =
                            piece.pieceMoves(currentBoard, position);

                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKing(
            ChessBoard currentBoard,
            TeamColor teamColor) {

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position =
                        new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType()
                        == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    private void makeMoveOnBoard(ChessBoard currentBoard, ChessMove move) {
        ChessPiece piece = currentBoard.getPiece(move.getStartPosition());

        if (piece != null &&
                piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                currentBoard.getPiece(move.getEndPosition()) == null) {

            ChessPosition capturedPawn =
                    new ChessPosition(move.getStartPosition().getRow(),
                            move.getEndPosition().getColumn());

            currentBoard.addPiece(capturedPawn, null);
        }

        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        currentBoard.addPiece(move.getEndPosition(), piece);
        currentBoard.addPiece(move.getStartPosition(), null);
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(position);

                if (piece != null) {
                    ChessPiece newPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    copy.addPiece(position, newPiece);
                }
            }
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame)) {
            return false;
        }

        ChessGame other = (ChessGame) o;
        return teamTurn == other.teamTurn &&
                Objects.equals(board, other.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}