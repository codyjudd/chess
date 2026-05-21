package chess;

import java.util.Objects;

public class ChessPosition {

    private int row;
    private int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ChessPosition)) {
            return false;
        }

        ChessPosition other = (ChessPosition) o;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row,col);
    }
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
