# M6 Submission
Alex Castaneda Hernandez

## 1. Who decides, who shows

MoveGenerator.status() detects checkmate using isInCheck() and legalMoves(), because deciding whether a position is checkmate is a game rule. 
Game.status() exposes that result to the controller. GameController.announceResultIfOver() asks the game for its status and passes the result message to the view, matching the responsibility of running a request and telling the view. 
ConsoleView.showMessage() or EmojiView.showMessage() displays the message, which belongs to what the user sees.

## 2. The dependency rule

Main imports BoardView, ConsoleView, EmojiView, and GameController from the view package to connect the application. 
GameController uses BoardView and PlayerAction without imports cuz they are in the same package.
Classes in engine, model, and factory must not import view classes, because chess rules and board state should not depend on how the game is displayed or how input is collected.

## 3. Adding a Swing view in M7

I expect to add a new class implementing BoardView for the Swing interface and update Main.java to select it. 
The new view would send button actions to GameController.handle() instead of calling runLoop(). 
GameController and the engine should not need changes because the controller already handles PlayerAction requests and communicates through the BoardView interface.