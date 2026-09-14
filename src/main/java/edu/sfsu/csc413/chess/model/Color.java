package edu.sfsu.csc413.chess.model;

/**
 * The two sides in a game of chess.
 *
 * <p>An enum rather than a boolean or an int: the compiler now rejects
 * meaningless values, and {@code switch} statements over it can be checked for
 * exhaustiveness.
 *
 * <p>The four method contracts below are fixed — later milestones call them.
 * The bodies are yours to write: that is M0b.
 */
public enum Color {
    WHITE,
    BLACK;

    /** The side whose turn it is after this one moves. */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    /**
     * The direction pawns of this color advance, measured in ranks.
     * White moves up the board (+1), black moves down (-1).
     */
    public int pawnDirection() {
        return this == WHITE ? 1 : -1;
    }

    /** The rank pawns of this color start on (0-based). */
    public int pawnStartRank() {
        return this == WHITE ? 1 : 6;
    }
    /** The rank a pawn of this color must reach to promote (0-based). */
    public int promotionRank() {
        return this == WHITE ? 7 : 0;
    }
}
