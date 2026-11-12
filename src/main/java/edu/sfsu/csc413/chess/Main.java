package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;

/** Creates the game, selects a view, and starts the controller. */
public final class Main {

    private Main() {
    }

    /**
     * Starts an interactive chess game.
     *
     * @param args use "emoji" as the first argument for chess figures
     */
    public static void main(String[] args) {
        Game game = new Game();

        BoardView view;
        if (args.length > 0 && args[0].equalsIgnoreCase("emoji")) {
            view = new EmojiView();
        } else {
            view = new ConsoleView();
        }

        GameController controller = new GameController(game, view);
        controller.start();
    }
}