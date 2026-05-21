package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;





public class ChessPiece {

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }


    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }


    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }


    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (type == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }
        ;
        if (type == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        }
        if (type == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        }

        if (type == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        }
        if (type == PieceType.KING) {
            return kingMoves(board, myPosition);
        }

        if (type == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }

        return new ArrayList<>();
    }


    private Collection<ChessMove> bishopMoves (ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        addLine(board, start, moves, 1, 1);
        addLine(board, start, moves, 1, -1);
        addLine(board, start, moves, -1, 1);
        addLine(board, start, moves, -1, -1);


        return moves;
    }


    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        addLine(board, start, moves, 1, 0);
        addLine(board, start, moves, -1, 0);
        addLine(board, start, moves, 0, 1);
        addLine(board, start, moves, 0, -1);

        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        addLine(board, start, moves, 1, 0);
        addLine(board, start, moves, -1, 0);
        addLine(board, start, moves, 0, 1);
        addLine(board, start, moves, 0, -1);

        addLine(board, start, moves, 1, 1);
        addLine(board, start, moves, 1, -1);
        addLine(board, start, moves, -1, 1);
        addLine(board, start, moves, -1, -1);

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        addOne(board, start, moves, 2, 1);
        addOne(board, start, moves, 2, -1);
        addOne(board, start, moves, -2, 1);
        addOne(board, start, moves, -2, -1);
        addOne(board, start, moves, 1, 2);
        addOne(board, start, moves, 1, -2);
        addOne(board, start, moves, -1, 2);
        addOne(board, start, moves, -1, -2);

        return moves;
    }

    ;

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        addOne(board, start, moves, 1, 0);
        addOne(board, start, moves, -1, 0);
        addOne(board, start, moves, 0, 1);
        addOne(board, start, moves, 0, -1);
        addOne(board, start, moves, 1, 1);
        addOne(board, start, moves, 1, -1);
        addOne(board, start, moves, -1, 1);
        addOne(board, start, moves, -1, -1);

        return moves;

    }


    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition start) {
        List<ChessMove> moves = new ArrayList<>();

        int direction = 1;
        int startRow = 2;
        int promotionRow = 8;

        if (pieceColor == ChessGame.TeamColor.BLACK) {
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }
        int row = start.getRow();
        int col = start.getColumn();

        int oneRow = row + direction;

        if (onBoard(oneRow, col)) {
            ChessPosition oneFoward = new ChessPosition (oneRow, col);

            if (board.getPiece(oneFoward) == null) {
                addPawnMove(moves, start, oneFoward, promotionRow);

                int twoRow = row + direction + direction;
                ChessPosition twoFoward = new ChessPosition(twoRow, col);

                if (row == startRow && onBoard(twoRow, col) && board.getPiece(twoFoward) == null) {
                    moves.add(new ChessMove(start, twoFoward, null));
                }
            }
        }

        checkPawnCapture(board, start, moves, oneRow, col -1, promotionRow);
        checkPawnCapture(board, start, moves, oneRow, col +1, promotionRow);

        return moves;
    }

    private void addLine(ChessBoard board, ChessPosition start, List<ChessMove> moves, int rowChange, int colChange) {
        int row = start.getRow() + rowChange;
        int col = start.getColumn() + colChange;

        while (onBoard(row,col)) {
            ChessPosition end = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(end);

            if(piece == null) {
                moves.add(new ChessMove(start,end, null ));
            }
            else {
                if (piece.getTeamColor() != pieceColor) {
                    moves.add(new ChessMove(start,end, null));
                }

                return;
            }

            row = row + rowChange;
            col = col + colChange;
        }
    }

    private void addOne(ChessBoard board, ChessPosition start, List<ChessMove> moves, int rowChange, int colChange) {
        int row = start.getRow() + rowChange;
        int col = start.getColumn() + colChange;

        if (onBoard(row, col)) {
            ChessPosition end = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(end);

            if (piece == null || piece.getTeamColor() != pieceColor) {
                moves.add (new ChessMove(start,end, null));
            }
        }
    }


    private void checkPawnCapture(ChessBoard baord, ChessPosition start, List<ChessMove> moves, int row, int col, int promotionRow) {

        if(onBoard(row, col)) {
            ChessPosition end = new ChessPosition(row,col);
            ChessPiece piece = baord.getPiece(end);

            if (piece != null && piece.getTeamColor() != pieceColor) {
                addPawnMove(moves, start, end, promotionRow);
            }
        }
    }

    private void addPawnMove(List <ChessMove> moves, ChessPosition start, ChessPosition end, int promotionRow) {
        if (end.getRow() == promotionRow) {
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        }
        else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private boolean onBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece)) {
            return false;
        }
        ChessPiece other = (ChessPiece) o;

        return pieceColor == other.pieceColor && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return pieceColor + " " + type;
    }

}
