package edu.sfsu.csc413.chess.model;

public enum PieceType {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    public static PieceType fromSymbol(char letter) {
        char uppercase = Character.toUpperCase(letter);

        for (PieceType type : values()) {
            if (type.symbol() == uppercase) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown piece symbol: " + letter);
    }
}
