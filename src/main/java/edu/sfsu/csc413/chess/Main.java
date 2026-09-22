package edu.sfsu.csc413.chess;

/**
 * Entry point.
 *
 * <p>At M0 this does nothing but prove the toolchain works. It grows into the
 * real launcher as the engine appears underneath it.
 */

import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;
import edu.sfsu.csc413.chess.view.PieceGlyphs;
import edu.sfsu.csc413.chess.view.TextBoardRenderer;

public final class Main {
    public static void main(String[] args) {
        Board board = new Board();

        PieceType[] backRank = {
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK
        };

        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            board.place(new Position(file, 0),
                    new Piece(Color.WHITE, backRank[file]));

            board.place(new Position(file, 1),
                    new Piece(Color.WHITE, PieceType.PAWN));

            board.place(new Position(file, 6),
                    new Piece(Color.BLACK, PieceType.PAWN));

            board.place(new Position(file, 7),
                    new Piece(Color.BLACK, backRank[file]));
        }

        System.out.println(
                new TextBoardRenderer(PieceGlyphs.LETTERS).render(board)
        );
    }

    private Main() {
    }
}
