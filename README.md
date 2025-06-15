# Pac‑Man Game

## 🎮 Game Features
- **Classic Pac‑Man mechanics**: Navigate the maze, eat food (10 points), power‑food (50 points), and bonus cherries (100 points).
- **Ghost behavior**: Four ghosts patrol randomly, reverse on walls, and return to start when eaten in power‑food mode (+200 points).
- **Power‑ups**: Eating a power‑food *scares* ghosts for 8 seconds—ghosts change appearance and can be eaten.
- **Cherry spawn/animation**: Periodic bonus cherries spawn every 10 seconds on random food tiles, alternating between two images for a simple animation effect.
- **Screen wrap-around**: Exiting left/right edges teleports Pac‑Man to the opposite side seamlessly.
- **Game states**:
  - **Playing**: Default state; HUD shows lives and score.
  - **Paused**: Press **P** to pause/resume. Semi‑transparent overlay + “PAUSED” text.
  - **Game Over**: After losing all lives, game halts. Overlay displays “GAME OVER” + final score, and the restart hint.

## 🔄 Programming Paradigms & Techniques
- **OOP Principles**: Encapsulation (`Block` class), single-responsibility methods (`loadMap`, `move`, `draw`), and separation of concerns between rendering, input, and logic.
- **Data Structures**: `HashSet<Block>` for efficient tracking and removal of dynamic entities.
- **Event-driven programming**: Swing event loop (`Timer` + `ActionListener`) triggers `move()` and `repaint()`.
- **Collision detection**: Axis-Aligned Bounding Boxes (AABB) via overlapping rectangles.
- **State management**: Booleans (`paused`, `gameOver`, `ghostsScared`) govern behavior and rendering states.
- **Timing logic**: Java's `System.currentTimeMillis()` to manage temporary states and spawn intervals.
- **Image handling**: `ImageIcon` and `getResource()` to load assets; dynamic image swapping for states and animations.
- **Game loop optimization**: `repaint()` only when necessary, using `System.currentTimeMillis()` to control update frequency.
- **Code organization**: Clear separation of concerns, concise method names, and consistent naming conventions.

![image](https://github.com/user-attachments/assets/e3e5129b-2422-4025-80c7-fda3ac95a1d7)
![image](https://github.com/user-attachments/assets/131761ba-137a-406a-8d70-c4bcfa9b226c)
![image](https://github.com/user-attachments/assets/b0e8e7ea-c6f7-47b7-ba0c-e2990a6c8a51)
when cherry eaten - ![image](https://github.com/user-attachments/assets/7c7e5999-39b2-48be-be58-499678704482)


## 🧩 Code Structure & OOP Concepts

### `App.java`
- Sets up the Swing `JFrame` container, configures window size, and attaches the `PacMan` panel.
- Calls `pack()`, centers window and requests focus to enable key input handling.

### `PacMan.java`
- Extends `JPanel` and implements `ActionListener` + `KeyListener`, combining rendering, game loop control, and input handling.
- Uses `javax.swing.Timer` for a consistent 20 FPS update cycle (`actionPerformed()`).

#### Inner Class: `Block`
- Represents moving/fixed game entities (Pac‑Man, ghosts, walls, food, cherries).
- Fields: position (`x`, `y`), size, `Image`, start position, movement direction & velocity.
- Methods:
  - `updateDirection(char)`: changes movement, applies collision correction.
  - `updateVelocity()`: recalculates velocity vector.
  - `reset()`: teleports back to spawn location.

#### Game Board Initialization (`loadMap()`)
- Uses a tile map (`String[]`) to lay out walls (`X`), food, power‑food (`*`), ghost spawn positions (`b`, `o`, `p`, `r`), and initial Pac‑Man (`P`).
- Dynamically builds `HashSet<Block>` collections for each type.

#### Rendering (`paintComponent()` & `draw(Graphics)`)
- Clears background and draws all active blocks in correct order: Pac‑Man → ghosts → walls → food → power‑food → cherries.
- Renders HUD and overlay messages depending on game state.
- Uses `Graphics2D` alpha blending for overlays.

#### Game Logic (`move()`)
- Moves Pac‑Man, wraps at horizontal edges.
- Checks wall collisions and undoes movement if blocked.
- Ghost movement: random directions, bounce off walls/sides; resets on collision with Pac‑Man.
- Food eating removes food, updates score, reloads map when empty.
- Power‑food eating triggers scared mode: changes ghost image, activates timed state.
- Scared-ghost-mode ends after a set duration; reverts ghost visuals.
- Cherry spawning: randomly chooses empty tile, animates via toggling between two images; removed upon collision.

#### Input Handling (`keyReleased(KeyEvent)`)
- Direction control via arrow keys (`updateDirection`).
- Press **P** toggles pause mode, halting movement/timer updates.
- Any key press when game over resets map, lives, score, positions, and restarts the loop.
