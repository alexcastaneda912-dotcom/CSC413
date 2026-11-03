# M5 Engine and Model Design

## Class diagram

This diagram shows every class in the engine and model packages.
BoardFactory is included only as an external dependency of Game.

Public constructors and methods are listed. Parameter types are shown
without parameter names to keep the boxes narrower. The six concrete
piece subclasses omit their methods, as permitted by the assignment.
Record accessors and generated equality methods are included.
Standard inherited Object methods and generated enum methods are omitted.

```mermaid
classDiagram
    direction TB

    namespace engine {
        class Game {
            -Board board
            -List~Move~ history
            -Color sideToMove
            +Game()
            +Game(Board, Color)
            +board() Board
            +sideToMove() Color
            +history() List~Move~
            +legalMoves() List~Move~
            +findLegalMove(String) Optional~Move~
            +play(Move) void
            +undoLastMove() Optional~Move~
        }

        class MoveGenerator {
            -MoveGenerator()
            +legalMoves(Board, Color) List~Move~$
            +isInCheck(Board, Color) boolean$
            +isAttacked(Board, Position, Color) boolean$
            ~pseudoLegalMoves(Board, Color) List~Move~$
        }
    }

    namespace model {
        class Board {
            -Piece[][] squares
            +Board()
            +pieceAt(Position) Piece
            +isEmpty(Position) boolean
            +place(Position, Piece) void
            +apply(Move) void
            +undo(Move) void
            +positionsOf(Color) List~Position~
            +kingPosition(Color) Position
            +toString() String
        }

        class Piece {
            <<abstract>>
            -Color color
            -PieceType type
            +color() Color
            +type() PieceType
            +symbol() char
            +pseudoLegalMoves(Board, Position) List~Move~*
            +attacks(Board, Position, Position) boolean
            +toString() String
        }

        class Pawn
        class Knight
        class Bishop
        class Rook
        class Queen
        class King

        class Move {
            <<record>>
            +Move(Position, Position, Piece, Piece, PieceType)
            +from() Position
            +to() Position
            +moved() Piece
            +captured() Piece
            +promotesTo() PieceType
            +quiet(Position, Position, Piece) Move$
            +capture(Position, Position, Piece, Piece) Move$
            +promotion(Position, Position, Piece, Piece, PieceType) Move$
            +isCapture() boolean
            +isPromotion() boolean
            +equals(Object) boolean
            +hashCode() int
            +toString() String
        }

        class Position {
            <<record>>
            +Position(int, int)
            +file() int
            +rank() int
            +isOnBoard(int, int) boolean$
            +parse(String) Position$
            +offsetOrNull(int, int) Position
            +equals(Object) boolean
            +hashCode() int
            +toString() String
        }

        class Color {
            <<enumeration>>
            WHITE
            BLACK
            +opposite() Color
            +pawnDirection() int
            +pawnStartRank() int
            +promotionRank() int
        }

        class PieceType {
            <<enumeration>>
            PAWN
            KNIGHT
            BISHOP
            ROOK
            QUEEN
            KING
            +symbol() char
            +fromSymbol(char) PieceType$
        }
    }

    namespace factory {
        class BoardFactory {
            <<external>>
        }
    }

    Game --> "1" Board : board
    Game --> "0..*" Move : history
    Game --> "1" Color : sideToMove
    Game ..> MoveGenerator : calls
    Game ..> BoardFactory : creates starting board

    MoveGenerator ..> Board : queries and restores
    MoveGenerator ..> Piece : asks for moves and attacks
    MoveGenerator ..> Move : filters
    MoveGenerator ..> Position : locates squares
    MoveGenerator ..> Color : selects sides

    Board --> "0..64" Piece : squares

    Piece <|-- Pawn
    Piece <|-- Knight
    Piece <|-- Bishop
    Piece <|-- Rook
    Piece <|-- Queen
    Piece <|-- King

    Piece --> "1" Color : color
    Piece --> "1" PieceType : type

    Move --> Position : from and to
    Move --> Piece : moved and captured
    Move --> "0..1" PieceType : promotesTo
```

### Relationship notes

- Solid arrows represent stored references. Dashed arrows represent
  dependencies used during operations.
- Hollow triangle arrows point from a subclass to its superclass.
- Game stores a Board reference, a list of Move references, and the
  current Color. It calls MoveGenerator but does not store one.
- Stored references are shown as associations rather than exclusive
  lifecycle ownership. Game can receive an existing Board, and Move
  records and Piece objects can be referenced elsewhere.
- MoveGenerator has no fields. Its operations are static and its
  constructor is private.
- The package-private pseudoLegalMoves method is included to show its
  restricted visibility.
- Each Move refers to one from position, one to position, and one moved
  piece. Its captured piece and promotion type may be null.
- Dependencies already evident from method signatures are not all
  repeated as arrows. For example, Piece reads Board and generates Move
  objects, while Board accepts Move objects in apply and undo.
- Board's existing promotion helper constructs Queen, Rook, Bishop, or
  Knight objects. These construction dependencies are described here
  rather than drawn as four additional crossing arrows. Board does not
  depend on PieceFactory.
- No BoardView or ReadOnlyBoard interface has been implemented.
- A trailing dollar sign marks a static method. A trailing asterisk marks
  an abstract method. The visibility markers are + for public, - for
  private, and ~ for package-private.

## Sequence diagram: game.legalMoves()

This diagram follows normal execution of the M5 implementation.
One Piece lifeline represents the different pieces consulted.
The caller's board is restored after each candidate.

```mermaid
sequenceDiagram
    actor Caller
    participant G as Game
    participant MG as MoveGenerator
    participant B as Board
    participant P as Piece

    Caller->>G: legalMoves()
    G->>MG: legalMoves(board, sideToMove)
    MG->>MG: pseudoLegalMoves(board, color)
    MG->>B: positionsOf(color)
    B-->>MG: source positions

    loop Each source position
        MG->>B: pieceAt(from)
        B-->>MG: piece
        MG->>P: pseudoLegalMoves(board, from)
        Note over B,P: Piece movement logic reads board occupancy
        P-->>MG: piece's pseudo-legal moves
        MG->>MG: addAll(piece moves)
    end

    Note over MG: Candidate list is complete before filtering

    loop Each candidate move
        MG->>B: apply(move)
        B-->>MG: board updated

        MG->>MG: isInCheck(board, color)
        MG->>B: kingPosition(color)
        Note over B,P: Board scans that color's positions and checks piece.type()
        B-->>MG: king position or null

        alt No king exists for color
            MG->>MG: inCheck = false
        else King exists
            Note over MG: attacker = color.opposite()
            MG->>MG: isAttacked(board, king, attacker)
            MG->>B: positionsOf(attacker)
            B-->>MG: attacking-side positions

            loop Attacker positions until an attack is found or all are checked
                MG->>B: pieceAt(from)
                B-->>MG: attacking piece
                MG->>P: attacks(board, from, king)
                Note over B,P: Piece supplies attack behavior and Pawn checks diagonals
                P-->>MG: true or false
            end

            MG->>MG: inCheck = attack found
        end

        Note over MG,B: finally always undoes the applied candidate
        MG->>B: undo(move)
        B-->>MG: original board restored

        alt King is not in check
            MG->>MG: add candidate to legal moves
        else King is in check
            MG->>MG: discard candidate
        end
    end

    MG-->>G: legal moves
    G-->>Caller: legal moves
```

If the check calculation throws an exception, the finally block still
calls Board.undo before the exception propagates. On normal execution,
the keep-or-drop decision happens after undo.

## Verification

- Maven: 61 tests run, 0 failures, 0 errors, 0 skipped.
- Game example: f2f3, e7e5, g2g4, d8h4.
- After those moves, game.legalMoves() returned an empty list.