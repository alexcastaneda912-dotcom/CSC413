package edu.sfsu.csc413.chess.engine;

import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the moves a side may play in a given position.
 * King safety is added in M5.
 */
public final class MoveGenerator {

    private MoveGenerator() {
    }

    /**
     * Every move color may legally play in the given position.
     * At M4, these are the pseudo-legal moves without filtering.
     */
    public static List<Move> legalMoves(Board board, Color color) {
        return pseudoLegalMoves(board, color);
    }

    /**
     * Every geometrically possible move for color, king safety aside.
     * Package-private so callers outside engine use Game.
     */
    static List<Move> pseudoLegalMoves(Board board, Color color) {
        List<Move> moves = new ArrayList<>();

        for (Position from : board.positionsOf(color)) {
            moves.addAll(board.pieceAt(from).pseudoLegalMoves(board, from));
        }

        return moves;
    }
}