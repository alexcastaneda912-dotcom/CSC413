# M7 Submission

## Student and tools

Alex Castaneda Hernandez  
Working alone.

I used ChatGPT with GPT-6 Astra Medium in three separate, fresh chats.
Each run started from the same project baseline on its own Git branch.
I copied the generated files into IntelliJ and ran the checks locally.

ChatGPT also helped compare the runs, explain Git commands, revise Undo
availability, change the selection color, guide the debugger check,
and prepare this write-up.

## Branches and controlled changes

| Run | Branch | Generated-code commit | Change from previous run |
| --- | --- | --- | --- |
| 1 | `m7-run-1` | `0283429` | Initial request |
| 2 | `m7-run-2` | `72deaee` | Request a four-choice promotion dialog instead of allowing automatic queen promotion |
| 3 | `m7-run-3` | `088efbe` | Add legal-destination highlighting using `game.legalMoves()` |

The model stayed the same. Runs 2 and 3 were independent generations
from the baseline, not additions to the preceding run's code.

I preferred run 3 and merged it into `main`. I kept the other run
branches for comparison.

## Instructions given to the agent

The complete instructions are in the committed root `AGENTS.md`.

They restrict generated code to new views and the necessary view
selection changes in `Main.java`. They prohibit changes to the model,
engine, factory, controller, existing view interfaces, existing views,
dependencies, and existing tests.

Player requests must go through `controller.handle(...)` using
`PlayerAction`. Chess rules must come from the model, not be
reimplemented in a view.

With ChatGPT assistance, I adapted the professor's instructions:

- The view selection rule refers to `Main.main`, because my project
  does not have a `chooseView` method.
- The window rule prohibits waiting for the next move or calling
  `controller.runLoop()`, but permits a modal dialog asking one
  question, such as a promotion choice.
- The agent must inspect source files instead of guessing APIs.
- The agent must report which checks actually ran rather than claim
  that an unopened window was tested.
- If a protected file appears to need a change, the agent must stop
  and ask instead of changing it.

The dialog clarification matters because asking which promotion piece
to use is different from running a blocking game loop inside the view.

## Requests

### Run 1

```text
This is M7 run 1 for my CSC413 chess project. Follow the AGENTS.md instructions above.

Generate a Swing view selectable with the command-line argument "swing":
- An 8×8 board with pieces readable on both square colors.
- Click a piece, then its destination, to request a move.
- Display whose turn it is, check, checkmate, stalemate, and illegal-move messages.
- Include an Undo button that sends PlayerAction.Undo to the controller.
- Support promotion; always choosing a queen is acceptable.
- Its start method builds the window and returns without calling controller.runLoop().

Also generate a separate, non-Swing BoardView selectable with "border".
It should display a bordered terminal board from the side-to-move perspective
and allow playing through the controller until the game ends.

Preserve the existing console and emoji options in Main.
Do not change any protected files or existing tests.

I am using chat and IntelliJ, so you cannot access my local repository.
Before generating code, ask for the source files needed to confirm the APIs.
Then provide complete new files, the required Main change, and checks for me to run.
Do not perform my required "M7 by hand" change.
```

### Run 2

The request was identical to run 1 except that the opening identified
run 2 and the promotion instruction was replaced with:

```text
- Support promotion by letting the player choose queen, rook, bishop, or knight using a dialog.
```

### Run 3

The request was identical to run 2 except that the opening identified
run 3 and this instruction was added:

```text
- Highlight legal destination squares for the selected piece using game.legalMoves(), without calculating chess rules in the view.
```

## Comparison and selection

| Feature | Run 1 | Run 2 | Run 3 |
| --- | --- | --- | --- |
| Promotion | Automatic queen | Four-choice dialog | Four-choice dialog with cancellation |
| Legal destinations | Not highlighted | Not highlighted | Highlighted using model-provided legal moves |
| Piece display | Text labels | Text labels | Contrasting piece badges |
| Undo after game over | Disabled | Available | Originally disabled |
| Additional view | Bordered terminal board | Bordered terminal board | Bordered terminal board |

I preferred run 3 because the legal-destination highlights made it
easier to see where a selected piece could move. I also preferred its
piece display.

Run 3 stores the selected square in a `Position selected` field.
The click handler selects a piece or submits a destination.
The `render` method obtains highlighted destinations from
`game.legalMoves()` rather than calculating movement rules itself.

Run 2 stores pending move information for its promotion-choice
handling. Run 3 handles the promotion dialog within its click flow,
before sending the completed move notation to the controller.

Run 1's automatic queen promotion was simpler, but I preferred the
choice offered by runs 2 and 3.

For the changes I wanted, run 3 was straightforward to locate and edit:
button availability is in `render`, and selection colors are in
`Square.paintComponent`. I chose it for its behavior and organization,
not simply its file size.

## Code review

Paths below are relative to
`src/main/java/edu/sfsu/csc413/chess/`.

Line numbers refer to the selected implementation on `main`, except
where the run 2 branch is explicitly named.

### 1. Move requests — kept

Location: `view/SwingView.java`, line 152, inside `click(Position)`.

The view constructs move notation and calls
`controller.handle(new PlayerAction.Move(notation))`.
It does not call `game.play()` directly.

I checked this with source review, legal and illegal moves in the
window, and the debugger observation recorded below. I kept it because
the controller remains responsible for changing the game.

### 2. Legal-destination highlighting — kept

Location: `view/SwingView.java`, line 180, inside `render(Game)`.

The view loops over `game.legalMoves()`, filters by the selected
starting position, and collects destination positions. Square rendering
uses those destinations for highlighting.

I checked this by selecting pieces and observing the highlighted
squares. I kept it because the view asks the model for legal moves
instead of duplicating chess rules.

### 3. Promotion — kept

Location: `view/SwingView.java`, lines 118 and 131, inside
`click(Position)`.

The view identifies promotion candidates using legal moves and offers
queen, rook, bishop, and knight through a dialog. It appends the chosen
piece to the move notation before sending the request to the controller.

I tested all four promotion choices. I kept this instead of run 1's
automatic queen promotion because it gives the player a choice.

### 4. Undo availability — changed

Location: `view/SwingView.java`, line 207, inside `render(Game)`.

Run 3 originally disabled Undo whenever the game was over:

```java
undoButton.setEnabled(!status.isGameOver());
```

With ChatGPT assistance, I changed it to:

```java
undoButton.setEnabled(!game.history().isEmpty());
```

This disables Undo when there is no move history but allows it after
checkmate or stalemate. I verified Undo after checkmate and continued
playing afterward.

## Idea adopted from a discarded run

I adopted run 2's ability to Undo after the game ends.

Source: `m7-run-2`, `view/SwingView.java`, line 77 for the Undo request
and lines 183–187 for the window's game-over handling.

Applied location: `main`, `view/SwingView.java`, line 207.

I did not replace run 3 with run 2. I changed run 3's button condition
while keeping its existing controller-based Undo listener.

I tested the change with Fool's Mate:

1. `f2f3`
2. `e7e5`
3. `g2g4`
4. `d8h4`

After checkmate, I clicked Undo. The queen returned to d8, the game
was no longer over, and it was Black's turn. I then successfully
played `b8c6`.

The change is saved in commit `5385f32`.

## Other view

`BorderView` is a separate, non-Swing implementation selected with
the command-line argument `border`.

It displays a bordered terminal board that rotates to the side to
move. Player input goes through the controller. I tested rotation
and played through checkmate.

The original default console view and the `emoji` option remain
available.

## Verification

For run 3, all 83 existing tests passed.

I manually checked:

- Selecting pieces and highlighting legal destinations.
- Legal and illegal moves.
- Undo.
- Check and checkmate.
- Stalemate.
- Queen, rook, bishop, and knight promotion.
- Bordered-view rotation and gameplay through checkmate.
- Original console and emoji options.

After changing Undo availability, I ran `./mvnw test` successfully
again and tested continuing play after undoing checkmate.

The run reviews included committed, staged, unstaged, and untracked
file checks. No protected source files or existing tests were changed
in the generated runs.

## Debugger evidence

I placed a breakpoint at `view/SwingView.java`, line 152, before:

```java
controller.handle(new PlayerAction.Move(notation));
```

I predicted that clicking e2 and then e4 would produce the notation
`"e2e4"` before the controller moved the pawn.

The debugger paused with:

- `position = "e4"`
- `notation = "e2e4"`
- `promotion = false`
- `selected = null`, because line 150 had already cleared it.

The board still displayed the pawn on e2. After I resumed execution,
the pawn moved to e4 and the status changed to Black to move.

This matched my prediction and showed the move request passing from
the view to the controller. This was a trace of a working move, not
a claim that I found and fixed a new bug.

## M7 by hand

I changed the selected-square highlight from gold to purple in
`view/SwingView.java`, line 319, inside `Square.paintComponent`.

I confirmed that selecting the e2 pawn displayed a purple border.
This is also visible in the paused debugger screenshot.

The change is saved in commit `9154be0`.

## Final implementation commits

- Selected run 3: `088efbe`.
- Adopted run 2's Undo behavior: `5385f32`.
- Changed the selection highlight to purple: `9154be0`.