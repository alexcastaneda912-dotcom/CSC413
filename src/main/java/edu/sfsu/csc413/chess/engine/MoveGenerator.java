package edu.sfsu.csc413.chess.engine;

import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates moves and checks king safety in a given position.
 * Holds no state; all required information arrives as parameters.
 */
public final class MoveGenerator {

    private MoveGenerator() {
    }

    /**
     * Returns every pseudo-legal move that does not leave color's
     * own king in check. Each candidate is applied and then undone,
     * leaving the board as it was before this method was called.
     *
     * @param board the board to examine
     * @param color the side whose legal moves to generate
     * @return the moves that preserve that side's king safety
     */
    public static List<Move> legalMoves(Board board, Color color) {
        List<Move> moves = new ArrayList<>();

        for (Move move : pseudoLegalMoves(board, color)) {
            board.apply(move);

            boolean inCheck;
            try {
                inCheck = isInCheck(board, color);
            } finally {
                board.undo(move);
            }

            if (!inCheck) {
                moves.add(move);
            }
        }

        return moves;
    }

    /**
     * Returns whether color's king is attacked by the opposite color.
     * A side without a king is considered not in check.
     *
     * @param board the board to examine
     * @param color the side whose king to check
     * @return true if that side's king is attacked, otherwise false
     */
    public static boolean isInCheck(Board board, Color color) {
        Position king = board.kingPosition(color);

        if (king == null) {
            return false;
        }

        return isAttacked(board, king, color.opposite());
    }

    /**
     * Returns whether any piece of the attacking color attacks target.
     * Uses each piece's attacks method so pawn attacks are distinguished
     * from forward pawn movement.
     *
     * @param board the board to examine
     * @param target the square to check
     * @param attacker the color of the attacking pieces
     * @return true if any attacking piece attacks the target
     */
    public static boolean isAttacked(
            Board board, Position target, Color attacker) {

        for (Position from : board.positionsOf(attacker)) {
            if (board.pieceAt(from).attacks(board, from, target)) {
                return true;
            }
        }

        return false;
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