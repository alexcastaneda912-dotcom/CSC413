package edu.sfsu.csc413.chess.model;

public class Piece {
        private final Color color;
        private final PieceType type;

        public Piece(Color color, PieceType type) {
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

        @Override
        public String toString() {
            return String.valueOf(symbol());
        }
    }

