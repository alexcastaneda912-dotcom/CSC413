package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] squares;

    public Board() {
        squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
    }

    public Piece pieceAt(Position position) {
        return squares[position.file()][position.rank()];
    }

    public boolean isEmpty(Position position) {
        return pieceAt(position) == null;
    }

    public void place(Position position, Piece piece) {
        squares[position.file()][position.rank()] = piece;
    }

    public void apply(Move move) {
        Piece arriving = move.isPromotion()
                ? createPromoted(move.promotesTo(), move.moved().color())
                : move.moved();

        place(move.from(), null);
        place(move.to(), arriving);
    }

    public void undo(Move move) {
        place(move.from(), move.moved());
        place(move.to(), move.captured());
    }

    /*
     * Keep promotion construction inside model to avoid a dependency
     * from model to factory. This duplicates four PieceFactory cases,
     * so changes to those constructors must be maintained in both places.
     */
    private Piece createPromoted(PieceType type, Color color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> throw new IllegalArgumentException(
                    "Invalid promotion type: " + type);
        };
    }

    public List<Position> positionsOf(Color color) {
        List<Position> positions = new ArrayList<>();

        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Piece piece = squares[file][rank];

                if (piece != null && piece.color() == color) {
                    positions.add(new Position(file, rank));
                }
            }
        }

        return positions;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();

        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {
            int emptyCount = 0;

            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Piece piece = squares[file][rank];

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        text.append(emptyCount);
                        emptyCount = 0;
                    }

                    text.append(piece.symbol());
                }
            }

            if (emptyCount > 0) {
                text.append(emptyCount);
            }

            if (rank > 0) {
                text.append('/');
            }
        }

        return text.toString();
    }
}