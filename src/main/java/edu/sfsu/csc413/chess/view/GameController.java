package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.PieceType;

import java.util.List;
import java.util.Optional;

/**
 * Connects player requests to the game and asks the view to display results.
 * The game owns the rules and position; the view owns input and output.
 */
public class GameController {

    private final Game game;
    private final BoardView view;
    private boolean quitRequested;

    public GameController(Game game, BoardView view) {
        this.game = game;
        this.view = view;
    }

    /** Returns the game so a view can read it when drawing. */
    public Game game() {
        return game;
    }

    /**
     * Greets the player, draws the opening position, and hands control
     * to the view.
     */
    public void start() {
        view.showMessage("Welcome to Chess!");
        view.render(game);
        view.start(this);
    }

    /**
     * Requests and handles actions until the player quits or the game ends.
     * Only views that wait for input call this method.
     */
    public void runLoop() {
        while (!quitRequested && !game.isGameOver()) {
            handle(view.nextAction(game));
        }
    }

    /**
     * Carries out one request, then redraws unless the player quits.
     * Announces the result and closes the view when the game ends.
     *
     * @param action the player's request
     */
    public void handle(PlayerAction action) {
        switch (action) {
            case PlayerAction.Move request -> playMove(request.notation());

            case PlayerAction.Undo request -> {
                if (game.undoLastMove().isEmpty()) {
                    view.showMessage("Nothing to undo.");
                }
            }

            case PlayerAction.ShowMoves request -> showLegalMoves();

            case PlayerAction.Quit request -> {
                quitRequested = true;
                view.showMessage("Goodbye.");
                view.close();
                return;
            }
        }

        view.render(game);
        announceResultIfOver();
    }

    /**
     * Finds and plays the requested move. If a four-character notation
     * does not match, asks the view for a promotion piece and retries once.
     */
    private void playMove(String notation) {
        Optional<Move> move = game.findLegalMove(notation);

        if (move.isEmpty() && notation.length() == 4) {
            PieceType choice = view.promotionChoice(game);
            String promotionNotation =
                    notation + Character.toLowerCase(choice.symbol());
            move = game.findLegalMove(promotionNotation);
        }

        if (move.isPresent()) {
            game.play(move.get());
        } else {
            view.showMessage("Not a legal move: '" + notation + "'.");
        }
    }

    /** Displays legal move notation in sorted order. */
    private void showLegalMoves() {
        List<String> notations = game.legalMoves().stream()
                .map(Move::toString)
                .sorted()
                .toList();

        view.showMessage(
                notations.size() + " legal moves: "
                        + String.join(" ", notations));
    }

    /** Announces a terminal result and releases the view. */
    private void announceResultIfOver() {
        GameStatus status = game.status();

        if (!status.isGameOver()) {
            return;
        }

        if (status == GameStatus.CHECKMATE) {
            view.showMessage(
                    "Checkmate. " + game.sideToMove().opposite() + " wins.");
        } else {
            view.showMessage("Stalemate. The game is a draw.");
        }

        view.close();
    }
}