package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardDrawer {

    public static void drawWhite(ChessBoard board) {
        drawBoard(board, false, null, null);
    }

    public static void drawBlack(ChessBoard board) {
        drawBoard(board, true, null, null);
    }

    public static void drawWhiteHighlight(ChessBoard board, ChessPosition selected, Collection<ChessMove> legalMoves) {
        drawBoard(board, false, selected, legalMoves);
    }

    public static void drawBlackHighlight(ChessBoard board, ChessPosition selected, Collection<ChessMove> legalMoves) {
        drawBoard(board, true, selected, legalMoves);
    }

    private static final String BORDER_BG   = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD;

    private static final String LIGHT_SQ     = SET_BG_COLOR_DARK_GREEN;
    private static final String DARK_SQ      = SET_BG_COLOR_LIGHT_GREY;
    private static final String HIGHLIGHT_SQ = SET_BG_COLOR_YELLOW;
    private static final String SELECTED_SQ  = SET_BG_COLOR_GREEN;

    private static final String WHITE_PIECE = SET_TEXT_COLOR_RED + SET_TEXT_BOLD;
    private static final String BLACK_PIECE = SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD;
    private static final String RESET       = RESET_BG_COLOR + RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT;

    private static final char[] COL_LABELS = {'a','b','c','d','e','f','g','h'};

    private static void drawBoard(ChessBoard board, boolean flipped,
                                  ChessPosition selected, Collection<ChessMove> legalMoves) {
        int[] rows = new int[8];
        int[] cols = new int[8];

        for (int i = 0; i < 8; i++) {
            rows[i] = flipped ? (i + 1) : (8 - i);
            cols[i] = flipped ? (8 - i) : (i + 1);
        }

        Set<ChessPosition> highlights = new HashSet<>();
        if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                highlights.add(move.getEndPosition());
            }
        }

        printColumnLabels(cols);

        for (int row : rows) {
            printRow(board, row, cols, selected, highlights);
        }

        printColumnLabels(cols);
        System.out.print(RESET);
        System.out.println();
    }

    private static void printColumnLabels(int[] cols) {
        System.out.print(BORDER_BG + BORDER_TEXT + "   ");
        for (int col : cols) {
            System.out.printf(" %c ", COL_LABELS[col - 1]);
        }
        System.out.println("   " + RESET);
    }

    private static void printRow(ChessBoard board, int row, int[] cols,
                                  ChessPosition selected, Set<ChessPosition> highlights) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + row + " " + RESET);

        for (int col : cols) {
            ChessPosition pos = new ChessPosition(row, col);
            String squareBg;

            if (selected != null && selected.equals(pos)) {
                squareBg = SELECTED_SQ;
            } else if (highlights.contains(pos)) {
                squareBg = HIGHLIGHT_SQ;
            } else {
                boolean light = (row + col) % 2 == 0;
                squareBg = light ? LIGHT_SQ : DARK_SQ;
            }

            System.out.print(squareBg + pieceSymbol(board, row, col) + RESET);
        }

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
