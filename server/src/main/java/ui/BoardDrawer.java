package ui;

import chess.*;

import static ui.EscapeSequences.*;


public class BoardDrawer {


    public static void drawWhite(ChessBoard board) {
        drawBoard(board, false);
    }


    public static void drawBlack(ChessBoard board) {
        drawBoard(board, true);
    }


    private static final String BORDER_BG   = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD;
    private static final String LIGHT_SQ    = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQ     = SET_BG_COLOR_GREEN ;
    private static final String WHITE_PIECE = SET_TEXT_COLOR_RED   + SET_TEXT_BOLD;
    private static final String BLACK_PIECE = SET_TEXT_COLOR_BLUE  + SET_TEXT_BOLD;
    private static final String RESET       = RESET_BG_COLOR + RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT;

    private static final char[] COL_LABELS = {'a','b','c','d','e','f','g','h'};

    private static void drawBoard(ChessBoard board, boolean flipped) {
        // Row iteration: white view = rows 8..1 top-to-bottom
        //                black view = rows 1..8 top-to-bottom
        int[] rows = new int[8];
        int[] cols = new int[8];
        for (int i = 0; i < 8; i++) {
            rows[i] = flipped ? (i + 1)     : (8 - i);
            cols[i] = flipped ? (8 - i)     : (i + 1);
        }

        printColumnLabels(cols);
        for (int row : rows) {
            printRow(board, row, cols);
        }
        printColumnLabels(cols);
        System.out.print(RESET);
    }

    private static void printColumnLabels(int[] cols) {
        System.out.print(BORDER_BG + BORDER_TEXT + "   ");
        for (int col : cols) {
            System.out.printf(" %c ", COL_LABELS[col - 1]);
        }
        System.out.println("   " + RESET);
    }

    private static void printRow(ChessBoard board, int row, int[] cols) {
        // the Left border
        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " " + RESET);

        for (int col : cols) {
            boolean lightSquare = (row + col) % 2 == 0;
            String squareBg = lightSquare ? LIGHT_SQ : DARK_SQ;
            System.out.print(squareBg + pieceSymbol(board, row, col) + RESET);
        }

        // the Right border
        System.out.println(BORDER_BG + BORDER_TEXT + " " + row + " " + RESET);
    }

    private static String pieceSymbol(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return EMPTY;
        }

        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isWhite ? WHITE_PIECE : BLACK_PIECE;

        String symbol = switch (piece.getPieceType()) {
            case KING   -> isWhite ? WHITE_KING   : BLACK_KING;
            case QUEEN  -> isWhite ? WHITE_QUEEN  : BLACK_QUEEN;
            case BISHOP -> isWhite ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK   -> isWhite ? WHITE_ROOK   : BLACK_ROOK;
            case PAWN   -> isWhite ? WHITE_PAWN   : BLACK_PAWN;
        };

        return color + symbol + RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT;
    }
}
