package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The pawn — the piece that breaks every rule the others follow.
 *
 * <p>It is the only piece that moves in just one direction, the only one whose
 * capture differs from its move, the only one with a special first move, and
 * the only one that turns into something else.
 *
 * <p>En passant is not handled here.
 */
public class Pawn extends Piece {

    /**
     * What a pawn may become on reaching the far rank.
     */
    private static final PieceType[] PROMOTION_CHOICES = {
            PieceType.QUEEN,
            PieceType.ROOK,
            PieceType.BISHOP,
            PieceType.KNIGHT
    };

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Move> pseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();

        int direction = color().pawnDirection();

        Position oneStep = from.offsetOrNull(0, direction);

        if (oneStep != null && board.pieceAt(oneStep) == null) {

            if (oneStep.rank() == color().promotionRank()) {
                for (PieceType promotionChoice : PROMOTION_CHOICES) {
                    moves.add(
                            Move.promotion(
                                    from,
                                    oneStep,
                                    this,
                                    null,
                                    promotionChoice
                            )
                    );
                }
            } else {
                moves.add(Move.quiet(from, oneStep, this));

                if (from.rank() == color().pawnStartRank()) {
                    Position twoStep = from.offsetOrNull(0, direction * 2);

                    if (twoStep != null && board.pieceAt(twoStep) == null) {
                        moves.add(Move.quiet(from, twoStep, this));
                    }
                }
            }
        }

        int[] captureFiles = {-1, 1};

        for (int fileDelta : captureFiles) {
            Position target = from.offsetOrNull(fileDelta, direction);

            if (target == null) {
                continue;
            }

            Piece occupant = board.pieceAt(target);

            if (occupant != null && occupant.color() != color()) {

                if (target.rank() == color().promotionRank()) {
                    for (PieceType promotionChoice : PROMOTION_CHOICES) {
                        moves.add(
                                Move.promotion(
                                        from,
                                        target,
                                        this,
                                        occupant,
                                        promotionChoice
                                )
                        );
                    }
                } else {
                    moves.add(Move.capture(from, target, this, occupant));
                }
            }
        }

        return moves;
    }

    @Override
    public boolean attacks(Board board, Position from, Position target) {
        int direction = color().pawnDirection();

        Position leftAttack = from.offsetOrNull(-1, direction);
        Position rightAttack = from.offsetOrNull(1, direction);

        return target.equals(leftAttack) || target.equals(rightAttack);
    }
}