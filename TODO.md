To recap, This is an **OOP + MVC JavaFX clone of the classic game Arkanoid**, structured with clear separation of **models**, **services**, **controllers**, and **views**.  
The game already includes:
- Full game loop and rendering logic.
- Functional menu and settings scenes (FXML + controllers).
- Power-ups, multi-ball, paddle, ball.
- Physics-based collision handling

Your job is to **extend this project to support multiple levels (Level 1 → Level 4)**, each with unique **brick layouts**.

Each level must have:
- A distinct brick layout (pattern or shape).
- Increasing difficulty (brick durability, spacing, or number).
- Automatic progression when the player clears all bricks.

#### 🔧 Implementation Guidelines:
- 1. Introduce a new `LevelController` class in src/main/java/com/game/arkanoid/controller that:
  - Handles loading level data and switching between levels.
  - Resets the `GameState` and populates the brick grid.
  - ... (i can only specify those two, add more things to do if you feel need to)
- The **`levels/`** resource directory under: src/main/resources/com/game/arkanoid/levels/
  stores a 2D brick layout using characters (e.g., `1, 2, 3 ,4` for different brick health(durability), `0` for empty) for each level (e.g., `level1.txt`, `level2.txt`, etc.) 

  - `BricksService` will be extended to read and build brick arrays from these layout files

- 2. Modify `GameService.java`
- After all bricks in the current level are destroyed, automatically trigger a transition to the next level.
- If the final level is cleared → show a simple **"Game Completed"** message and return to menu.


- Suggested addition:
    ```java
    if (bricksSvc.allBricksCleared(state.bricks)) {
        levelController.loadNextLevel(state);
    }
    ```
3. Update BricksService.java

- Add support for loading bricks from a text file rather than hardcoded loops. (modify Container.java from     hardcoding to load txt files)
examples:
        000111000
        001222100
        011333110
        111444111
→ 0 = empty, 1–4 = brick type (color, durability).
if we want to add more levels later, just add txt files in src/main/resources/com/game/arkanoid/levels/

4. Add 1up, highscore in the gameplay (reference in image.png), but just keep highscore as a placeholder, i will ask you to create a database for this game in later prompts(NOT IN THIS PROMPT)


5. ADDITIONAL modification for better code structure
- instead of defining a brick with its health, you might want to create:
BrickType enum under models/:
public enum BrickType {
    NORMAL, STRONG, UNBREAKABLE, BONUS, etc...
} 
and then modify relevant files as needed (BallService, BallRenderer, ...)


6. FINALLY, REMEMBER: keep the current structure, methods are highly reccommended. but feel free to add any help function as you needed, or even modify any function.
