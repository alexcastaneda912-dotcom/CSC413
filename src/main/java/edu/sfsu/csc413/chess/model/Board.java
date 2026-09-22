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
