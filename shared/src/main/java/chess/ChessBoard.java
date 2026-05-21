package chess;

import java.util.Arrays;



public class ChessBoard {

    private ChessPiece [][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }


    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() -1;
        int col = position.getColumn() -1;

        board[row][col] = piece;
    }


    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() -1;
        int col = position.getColumn() -1;

        return board[row][col];
    }


    public void resetBoard() {
        board = new ChessPiece[8][8];

        ChessPiece.PieceType[] order = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK,
        };

        for (int col =1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE, order[col -1]));

            addPiece(new ChessPosition(2, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE,ChessPiece.PieceType.PAWN));

            addPiece( new ChessPosition(7, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK,ChessPiece.PieceType.PAWN));

            addPiece(new ChessPosition(8, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, order[col- 1]));

        }
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessBoard)) {
            return false;
        }

        ChessBoard other = (ChessBoard) o;
        return Arrays.deepEquals(board, other.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board);
    }

}
