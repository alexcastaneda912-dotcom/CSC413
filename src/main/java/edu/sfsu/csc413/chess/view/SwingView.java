package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/** Event-driven view: all game requests go through the controller on the EDT. */
public final class SwingView implements BoardView {
    private final Square[][] squares = new Square[8][8];
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel messageLabel;
    private JButton undoButton;
    private GameController controller;
    private Game game;
    private Position selected;
    private String message = "Click a piece, then its destination.";
    private boolean quitting;

    @Override
    public void start(GameController controller) {
        onEdt(() -> {
            this.controller = controller;
            this.game = controller.game();

            frame = new JFrame("CSC413 Chess — Run 3");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    quitting = true;
                    controller.handle(new PlayerAction.Quit());
                }
            });

            JPanel board = new JPanel(new GridLayout(8, 8));

            for (int rank = 7; rank >= 0; rank--) {
                for (int file = 0; file < 8; file++) {
                    Position position = new Position(file, rank);
                    Square square = new Square(position);
                    square.addActionListener(event -> click(position));
                    squares[file][rank] = square;
                    board.add(square);
                }
            }

            statusLabel = new JLabel();
            messageLabel = new JLabel(message);
            undoButton = new JButton("Undo");

            undoButton.addActionListener(event -> {
                selected = null;
                showMessage(" ");
                controller.handle(new PlayerAction.Undo());
            });

            JPanel footer = new JPanel(new GridLayout(0, 1, 0, 5));
            footer.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            footer.add(statusLabel);
            footer.add(messageLabel);
            footer.add(new JLabel(
                    "White badges = White; dark badges = Black. N = knight."));
            footer.add(undoButton);

            frame.add(board, BorderLayout.CENTER);
            frame.add(footer, BorderLayout.SOUTH);

            render(game);
            frame.pack();
            frame.setMinimumSize(frame.getSize());
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private void click(Position position) {
        if (game.status().isGameOver()) {
            return;
        }

        Piece piece = game.board().pieceAt(position);

        if (selected == null) {
            if (piece == null || piece.color() != game.sideToMove()) {
                showMessage("Select a " + game.sideToMove() + " piece first.");
                return;
            }

            selected = position;
            showMessage("Selected " + position
                    + ". Click a destination; click again to cancel.");
            render(game);
            return;
        }

        if (selected.equals(position)) {
            selected = null;
            showMessage("Selection cleared.");
            render(game);
            return;
        }

        String notation = selected.toString() + position;

        // Inspect engine-provided moves instead of calculating promotion rules.
        boolean promotion = game.legalMoves().stream().anyMatch(move ->
                move.from().equals(selected)
                        && move.to().equals(position)
                        && move.isPromotion());

        if (promotion) {
            PieceType[] choices = {
                    PieceType.QUEEN,
                    PieceType.ROOK,
                    PieceType.BISHOP,
                    PieceType.KNIGHT
            };

            int choice = JOptionPane.showOptionDialog(
                    frame,
                    "Choose the promotion piece:",
                    "Promotion",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Queen", "Rook", "Bishop", "Knight"},
                    "Queen"
            );

            if (choice < 0) {
                showMessage("Promotion cancelled. Choose a destination again.");
                return;
            }

            notation += Character.toLowerCase(choices[choice].symbol());
        }

        selected = null;
        showMessage(" ");
        controller.handle(new PlayerAction.Move(notation));
    }

    @Override
    public void render(Game game) {
        onEdt(() -> {
            this.game = game;

            // The controller renders before calling start.
            if (frame == null) {
                return;
            }

            GameStatus status = game.status();

            String detail = switch (status) {
                case IN_PROGRESS -> "In progress";
                case CHECK -> "Check!";
                case CHECKMATE ->
                        "Checkmate. " + game.sideToMove().opposite() + " wins.";
                case STALEMATE -> "Stalemate. Draw.";
            };

            statusLabel.setText(game.sideToMove() + " to move | " + detail);

            Set<Position> destinations = new HashSet<>();

            if (selected != null) {
                for (Move move : game.legalMoves()) {
                    if (move.from().equals(selected)) {
                        destinations.add(move.to());
                    }
                }
            }

            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    Square square = squares[file][rank];
                    square.piece = game.board().pieceAt(square.position);
                    square.selected = square.position.equals(selected);
                    square.destination = destinations.contains(square.position);
                    square.setEnabled(!status.isGameOver());

                    String description = square.position + ": "
                            + (square.piece == null
                            ? "empty"
                            : square.piece.color() + " " + square.piece.type());

                    square.setToolTipText(description);
                    square.getAccessibleContext()
                            .setAccessibleName(description);
                    square.repaint();
                }
            }

            undoButton.setEnabled(!game.history().isEmpty());
        });
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
            if (frame == null) {
                return;
            }

            if (quitting || game == null || !game.status().isGameOver()) {
                frame.dispose();
            } else {
                // Leave the final board and result visible until the user closes it.
                selected = null;
                render(game);
            }
        });
    }

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while updating chess window", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Could not update chess window", exception.getCause());
        }
    }

    /** Contrasting badges keep pieces readable on either square color. */
    private static final class Square extends JButton {
        private final Position position;
        private Piece piece;
        private boolean selected;
        private boolean destination;

        private Square(Position position) {
            this.position = position;
            setPreferredSize(new Dimension(72, 72));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setColor((position.file() + position.rank()) % 2 == 0
                        ? new Color(110, 130, 145)
                        : new Color(235, 228, 211));

                g.fillRect(0, 0, getWidth(), getHeight());

                if (piece != null) {
                    int size = Math.min(getWidth(), getHeight()) - 24;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    boolean white = piece.color()
                            == edu.sfsu.csc413.chess.model.Color.WHITE;

                    g.setColor(white ? Color.WHITE : new Color(25, 25, 25));
                    g.fillOval(x, y, size, size);

                    g.setColor(white ? Color.BLACK : Color.WHITE);
                    g.setStroke(new BasicStroke(2));
                    g.drawOval(x, y, size, size);

                    g.setFont(new Font(
                            Font.SANS_SERIF, Font.BOLD, size / 2));

                    String letter = String.valueOf(piece.type().symbol());
                    FontMetrics metrics = g.getFontMetrics();

                    g.drawString(
                            letter,
                            (getWidth() - metrics.stringWidth(letter)) / 2,
                            (getHeight() - metrics.getHeight()) / 2
                                    + metrics.getAscent()
                    );
                }

                if (selected || destination || hasFocus()) {
                    g.setColor(selected
                            ? new Color(255, 190, 0)
                            : destination
                            ? new Color(0, 240, 110)
                            : Color.BLUE);

                    g.setStroke(new BasicStroke(4));
                    g.drawRect(3, 3, getWidth() - 7, getHeight() - 7);
                }
            } finally {
                g.dispose();
            }
        }
    }
}