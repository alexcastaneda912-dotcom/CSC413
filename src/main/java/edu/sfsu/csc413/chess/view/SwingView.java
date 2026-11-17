package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Event-driven view. All game access after startup happens on Swing's EDT. */
public final class SwingView implements BoardView {
    private static final java.awt.Color LIGHT =
            new java.awt.Color(240, 224, 195);
    private static final java.awt.Color DARK =
            new java.awt.Color(150, 175, 145);

    private final JButton[][] squares = new JButton[8][8];

    private JFrame window;
    private JLabel statusLabel;
    private JLabel messageLabel;
    private JButton undo;
    private GameController controller;
    private Game game;
    private Position selected;
    private String message = "Click a piece, then its destination.";
    private boolean stopped;
    private boolean disposing;

    private static void onEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    @Override
    public void start(GameController controller) {
        onEdt(() -> {
            this.controller = controller;
            this.game = controller.game();

            window = new JFrame("CSC413 Chess");
            window.setDefaultCloseOperation(
                    WindowConstants.DO_NOTHING_ON_CLOSE);

            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    disposing = true;
                    controller.handle(new PlayerAction.Quit());
                }
            });

            JPanel board = new JPanel(new GridLayout(8, 8));

            for (int row = 0; row < 8; row++) {
                for (int file = 0; file < 8; file++) {
                    Position position = new Position(file, 7 - row);
                    JButton square = new JButton();

                    square.setFont(
                            new Font(Font.SANS_SERIF, Font.BOLD, 22));
                    square.setPreferredSize(new Dimension(72, 72));
                    square.setOpaque(true);
                    square.setContentAreaFilled(true);
                    square.setFocusPainted(false);

                    square.addActionListener(
                            event -> clickSquare(position));

                    squares[file][7 - row] = square;
                    board.add(square);
                }
            }

            statusLabel = new JLabel();
            messageLabel = new JLabel(message);

            undo = new JButton("Undo");
            undo.addActionListener(event -> {
                selected = null;
                message = " ";
                controller.handle(new PlayerAction.Undo());
            });

            JPanel footer = new JPanel(new BorderLayout(8, 8));
            footer.add(messageLabel, BorderLayout.CENTER);
            footer.add(undo, BorderLayout.EAST);

            window.add(statusLabel, BorderLayout.NORTH);
            window.add(board, BorderLayout.CENTER);
            window.add(footer, BorderLayout.SOUTH);

            draw();
            window.pack();
            window.setLocationByPlatform(true);
            window.setVisible(true);
        });
    }

    private void clickSquare(Position position) {
        if (stopped) {
            return;
        }

        if (selected == null) {
            Piece piece = game.board().pieceAt(position);

            if (piece == null || piece.color() != game.sideToMove()) {
                showMessage(
                        "Select a piece belonging to "
                                + game.sideToMove() + ".");
                return;
            }

            selected = position;
            message = "Selected " + position + ". Click its destination.";
            draw();
        } else {
            // The controller validates the request and defaults
            // promotions to queen.
            String notation = selected.toString() + position;
            selected = null;
            message = " ";
            controller.handle(new PlayerAction.Move(notation));
        }
    }

    @Override
    public void render(Game game) {
        onEdt(() -> {
            this.game = game;
            selected = null;

            if (window != null) {
                draw();
            }
        });
    }

    private void draw() {
        GameStatus status = game.status();

        String detail = switch (status) {
            case IN_PROGRESS -> "Choose a move";
            case CHECK -> "Check";
            case CHECKMATE ->
                    "Checkmate. "
                            + game.sideToMove().opposite() + " wins.";
            case STALEMATE -> "Stalemate. Draw.";
        };

        statusLabel.setText(
                game.sideToMove() + " to move | " + detail);
        messageLabel.setText(message);

        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position position = new Position(file, rank);
                Piece piece = game.board().pieceAt(position);
                JButton square = squares[file][rank];

                // Explicit color letters avoid font-dependent chess glyphs.
                String text = piece == null ? "" :
                        (piece.color()
                                == edu.sfsu.csc413.chess.model.Color.WHITE
                                ? "w" : "b")
                                + piece.type().symbol();

                square.setText(text);
                square.setForeground(java.awt.Color.BLACK);
                square.setBackground(
                        (file + rank) % 2 == 0 ? DARK : LIGHT);

                square.setBorder(BorderFactory.createLineBorder(
                        position.equals(selected)
                                ? java.awt.Color.BLUE
                                : java.awt.Color.GRAY,
                        position.equals(selected) ? 4 : 1));

                square.setToolTipText(
                        position + (piece == null
                                ? " empty"
                                : " " + piece.color() + " " + piece.type()));

                square.getAccessibleContext().setAccessibleName(
                        square.getToolTipText());
            }
        }

        undo.setEnabled(!stopped);
    }

    @Override
    public void showMessage(String message) {
        onEdt(() -> {
            this.message = message;

            if (messageLabel != null) {
                messageLabel.setText(message);
            }
        });
    }

    @Override
    public void close() {
        onEdt(() -> {
            stopped = true;
            selected = null;

            if (window == null) {
                return;
            }

            if (disposing) {
                window.dispose();
            } else {
                // Keep the result visible until the player closes the window.
                draw();
            }
        });
    }
}