# Instructions for coding agents

This is a Java chess project using MVC.
The existing model and controller are frozen for M7.
Your job is to add views, not change the chess rules or controller.

## Do

- Add new view classes under
  `src/main/java/edu/sfsu/csc413/chess/view/`
  or a new package beneath it.
- Implement BoardView. Read BoardView, ConsoleView, PlayerAction,
  and GameController before writing a view.
- Read the game through Game: board(), sideToMove(), legalMoves(),
  status(), and history(). Treat the returned board as read-only.
- Send player requests through controller.handle using PlayerAction.
  For example: controller.handle(new PlayerAction.Move("e2e4")).
- Update the view selection in Main.main so new views can be selected
  from command-line arguments. This project has no chooseView method.
- Preserve the existing default console view and the emoji argument.
- Use only the JDK.

## Do not

- Do not change files in model/, engine/, or factory/.
- Do not change GameController, BoardView, PlayerAction,
  ConsoleView, or EmojiView.
- Do not change pom.xml.
- Do not change or delete existing tests.
- Do not call game.play(), game.undoLastMove(), or board mutation
  methods from a view. Only the controller changes the game.
- Do not implement chess rules in a view. Get legal moves from
  game.legalMoves() and check or game-over status from game.status().
- Do not wait for the next move inside the window. A Swing view's
  start method builds the window and returns; it never calls
  controller.runLoop(). A modal dialog asking one question,
  such as a promotion choice, is allowed.
- Do not implement the student's required "M7 by hand" change.

## Check your work

- Run ./mvnw test: at least 83 tests must pass, with no existing
  test changed or deleted.
- Inspect git diff --stat submit-m6 HEAD and any uncommitted changes.
- Allowed M7 changes are new view classes, new view tests,
  Main.java, AGENTS.md, optional CLAUDE.md, m7-submission.md,
  and the supplied docs/milestones/m7.md.
- Search new views for direct game mutation calls.
- Check that the Swing view does not call runLoop.
- Test Fool's Mate, an illegal move, Undo, and promotion.
- Check that every piece is readable on both square colors.
- Report which checks actually ran. Do not claim the window was
  tested if it was not opened.
- In a chat without repository access, ask for needed source files
  instead of guessing APIs, and give the user the checks to run.

## Stop and ask

If completing the request appears to require changing a protected
file, stop and explain why instead of changing it.