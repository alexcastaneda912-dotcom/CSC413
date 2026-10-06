package edu.sfsu.csc413.chess.engine;

import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One game of chess: the board, whose turn it is,
 * and the moves played so far.
 */
public class Game {

    private final Board board;

    private final List<Move> history = new ArrayList<>();

    private Color sideToMove;

    /**
     * Starts a game from the standard opening position.
     */
    public Game() {
        this(BoardFactory.standard(), Color.WHITE);
    }

    /**
     * Starts a game from a given position.
     */
    public Game(Board board, Color sideToMove) {
        this.board = board;
        this.sideToMove = sideToMove;
    }

    public Board board() {
        return board;
    }

    public Color sideToMove() {
        return sideToMove;
    }

    /**
     * Returns an unmodifiable copy of the moves played,
     * oldest first.
     */
    public List<Move> history() {
        return List.copyOf(history);
    }

    /**
     * Returns every pseudo-legal move for the current side.
     * King safety is added in M5.
     */
    public List<Move> legalMoves() {
        List<Move> moves = new ArrayList<>();

        for (Position from : board.positionsOf(sideToMove)) {
            moves.addAll(board.pieceAt(from).pseudoLegalMoves(board, from));
        }

        return moves;
    }

    /**
     * Finds a legal move matching long algebraic notation,
     * ignoring case. Returns Optional.empty() if none matches.
     */
    public Optional<Move> findLegalMove(String notation) {
        for (Move move : legalMoves()) {
            if (move.toString().equalsIgnoreCase(notation)) {
                return Optional.of(move);
            }
        }

        return Optional.empty();
    }

    /**
     * Plays a move and passes the turn.
     *
     * @throws IllegalArgumentException if the move is not currently legal
     */
    public void play(Move move) {
        if (!legalMoves().contains(move)) {
            throw new IllegalArgumentException("Illegal move: " + move);
        }

        board.apply(move);
        history.add(move);
        sideToMove = sideToMove.opposite();
    }

    /**
     * Takes back the most recent move.
     * Returns Optional.empty() if no moves have been played.
     */
    public Optional<Move> undoLastMove() {
        if (history.isEmpty()) {
            return Optional.empty();
        }

        Move move = history.remove(history.size() - 1);
        board.undo(move);
        sideToMove = sideToMove.opposite();

        return Optional.of(move);
    }
}