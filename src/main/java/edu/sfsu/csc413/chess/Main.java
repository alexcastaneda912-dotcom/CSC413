package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.BorderView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;
import edu.sfsu.csc413.chess.view.SwingView;

/** Creates the game, selects a view, and starts the controller. */
public final class Main {

    private Main() {
    }

    /**
     * Starts an interactive chess game.
     *
     * @param args first argument: swing, border, emoji, or console (default)
     */
    public static void main(String[] args) {
        Game game = new Game();

        BoardView view;

        if (args.length > 0 && args[0].equalsIgnoreCase("emoji")) {
            view = new EmojiView();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("swing")) {
            view = new SwingView();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("border")) {
            view = new BorderView();
        } else {
            view = new ConsoleView();
        }

        GameController controller = new GameController(game, view);
        controller.start();
    }
}
