package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }

    public char symbol() {
        if (color == Color.WHITE) {
            return type.symbol();
        }

        return Character.toLowerCase(type.symbol());
    }

    public abstract List<Move> pseudoLegalMoves(Board board, Position from);

    public boolean attacks(Board board, Position from, Position target) {
        for (Move move : pseudoLegalMoves(board, from)) {
            if (move.to().equals(target)) {
                return true;
            }
        }

        return false;
    }

    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        List<Move> moves = new ArrayList<>();

        for (int[] direction : directions) {
            int fileDelta = direction[0];
            int rankDelta = direction[1];

            Position to = from;

            while (true) {
                to = to.offsetOrNull(fileDelta, rankDelta);

                if (to == null) {
                    break;
                }

                Piece occupant = board.pieceAt(to);

                if (occupant == null) {
                    moves.add(Move.quiet(from, to, this));
                } else {
                    if (occupant.color() != color) {
                        moves.add(Move.capture(from, to, this, occupant));
                    }

                    break;
                }
            }
        }

        return moves;
    }

    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets) {
        List<Move> moves = new ArrayList<>();

        for (int[] offset : offsets) {
            int fileDelta = offset[0];
            int rankDelta = offset[1];

            Position to = from.offsetOrNull(fileDelta, rankDelta);

            if (to == null) {
                continue;
            }

            Piece occupant = board.pieceAt(to);

            if (occupant == null) {
                moves.add(Move.quiet(from, to, this));
            } else if (occupant.color() != color) {
                moves.add(Move.capture(from, to, this, occupant));
            }
        }

        return moves;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}