package edu.sfsu.csc413.chess.model;

public record Position (int file, int rank){

    public static final int BOARD_SIZE = 8;

    public static boolean isOnBoard(int file, int rank){
        return file >= 0 && file < BOARD_SIZE
                && rank >= 0 && rank < BOARD_SIZE;
    }

    public Position {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ", rank=" + rank);
        }
    }


    public static Position parse(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid position: " + algebraic);
        }

        int file = algebraic.charAt(0) - 'a';
        int rank = algebraic.charAt(1) - '1';

        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Invalid position: " + algebraic);
        }

        return new Position(file, rank);
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        int newFile = file + fileDelta;
        int newRank = rank + rankDelta;

        return isOnBoard(newFile, newRank)
                ? new Position(newFile, newRank)
                : null;
    }



    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }
}
