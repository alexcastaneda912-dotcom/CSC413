package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Event-driven board. All game reads and player requests happen on the EDT. */
public final class SwingView implements BoardView {
    private static final java.awt.Color LIGHT = new java.awt.Color(243, 231, 205);
    private static final java.awt.Color DARK = new java.awt.Color(142, 172, 192);
    private final JButton[][] squares = new JButton[8][8];
    private JFrame frame;
    private JLabel status;
    private JLabel messageLabel;
    private GameController controller;
    private Game game;
    private Position selected;
    private Position pendingFrom;
    private Position pendingTo;
    private String message = "Click a piece, then its destination.";

    private static void onEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) task.run();
        else SwingUtilities.invokeLater(task);
    }

    @Override
    public void start(GameController controller) {
        onEdt(() -> {
            this.controller = controller;
            this.game = controller.game();
            frame = new JFrame("CSC413 Chess — Run 2");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    controller.handle(new PlayerAction.Quit());
                    frame.dispose();
                }
            });
            JPanel board = new JPanel(new GridLayout(8, 8));
            for (int rank = 7; rank >= 0; rank--) {
                for (int file = 0; file < 8; file++) {
                    Position position = new Position(file, rank);
                    java.awt.Color background = (file + rank) % 2 == 0 ? DARK : LIGHT;
                    // Paint the background ourselves so native themes cannot hide it.
                    JButton square = new JButton() {
                        @Override
                        protected void paintComponent(Graphics graphics) {
                            graphics.setColor(background);
                            graphics.fillRect(0, 0, getWidth(), getHeight());
                            super.paintComponent(graphics);
                        }
                    };
                    square.setContentAreaFilled(false);
                    square.setForeground(java.awt.Color.BLACK);
                    square.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                    square.setPreferredSize(new Dimension(70, 70));
                    square.setMargin(new Insets(0, 0, 0, 0));
                    square.addActionListener(event -> click(position));
                    squares[file][rank] = square;
                    board.add(square);
                }
            }
            status = new JLabel();
            messageLabel = new JLabel(message);
            JButton undo = new JButton("Undo");
            undo.addActionListener(event -> {
                selected = null;
                showMessage(" ");
                controller.handle(new PlayerAction.Undo());
            });
            JPanel footer = new JPanel(new BorderLayout(10, 6));
            footer.add(messageLabel, BorderLayout.CENTER);
            footer.add(undo, BorderLayout.EAST);
            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            content.add(status, BorderLayout.NORTH);
            content.add(board, BorderLayout.CENTER);
            content.add(footer, BorderLayout.SOUTH);
            frame.setContentPane(content);
            render(game);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private void click(Position destination) {
        if (game.status().isGameOver()) return;
        Piece piece = game.board().pieceAt(destination);
        if (selected == null) {
            if (piece == null || piece.color() != game.sideToMove()) {
                showMessage("Select a " + game.sideToMove() + " piece first.");
                return;
            }
            selected = destination;
            showMessage("Selected " + selected + ". Click its destination.");
            render(game);
            return;
        }
        if (selected.equals(destination)) {
            selected = null;
            showMessage("Selection cleared.");
            render(game);
            return;
        }
        pendingFrom = selected;
        pendingTo = destination;
        selected = null;
        showMessage(" ");
        try {
            controller.handle(new PlayerAction.Move(pendingFrom.toString() + pendingTo));
        } finally {
            pendingFrom = null;
            pendingTo = null;
        }
    }

    @Override
    public PieceType promotionChoice(Game game) {
        // The frozen controller also calls this for ordinary illegal requests.
        boolean promotion = pendingFrom != null && pendingTo != null
                && game.legalMoves().stream().anyMatch(move -> move.isPromotion()
                && move.from().equals(pendingFrom) && move.to().equals(pendingTo));
        if (!promotion) return PieceType.QUEEN;
        PieceType[] choices = {PieceType.QUEEN, PieceType.ROOK,
                PieceType.BISHOP, PieceType.KNIGHT};
        int answer = JOptionPane.showOptionDialog(frame,
                "Choose a promotion piece (closing selects queen).", "Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Queen", "Rook", "Bishop", "Knight"}, "Queen");
        return answer < 0 ? PieceType.QUEEN : choices[answer];
    }

    @Override
    public void render(Game game) {
        onEdt(() -> {
            this.game = game;
            if (frame == null) return;
            String state = switch (game.status()) {
                case IN_PROGRESS -> game.sideToMove() + " to move";
                case CHECK -> game.sideToMove() + " to move — CHECK";
                case CHECKMATE -> "CHECKMATE — " + game.sideToMove().opposite() + " wins";
                case STALEMATE -> "STALEMATE — Draw";
            };
            status.setText(state + "  |  w = White, b = Black; N = Knight");
            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    Position position = new Position(file, rank);
                    Piece piece = game.board().pieceAt(position);
                    JButton square = squares[file][rank];
                    String label = piece == null ? "" :
                            (piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE ? "w" : "b")
                                    + piece.type().symbol();
                    square.setText(label);
                    square.setToolTipText(position + (piece == null ? " (empty)"
                            : " " + piece.color() + " " + piece.type()));
                    square.getAccessibleContext().setAccessibleName(square.getToolTipText());
                    square.setBorder(position.equals(selected)
                            ? BorderFactory.createLineBorder(java.awt.Color.BLUE, 4)
                            : BorderFactory.createEmptyBorder(4, 4, 4, 4));
                }
            }
        });
    }

    @Override
    public void showMessage(String message) {
        onEdt(() -> {
            this.message = message;
            if (messageLabel != null) messageLabel.setText(message);
        });
    }

    @Override
    public void close() {
        onEdt(() -> {
            // Keep the final result visible; Undo can reopen the position.
            // The native window close handler always disposes after Quit.
            if (frame != null && !game.status().isGameOver()) frame.dispose();
        });
    }
}