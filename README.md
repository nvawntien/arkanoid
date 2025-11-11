# 🎮 Arkanoid Game  
*A modern Object-Oriented remake of the classic brick-breaker arcade.*

---

## 🧩 Overview
**Arkanoid Game** is a fully object-oriented JavaFX project built to demonstrate advanced programming principles, modular architecture, and engaging gameplay mechanics.  
Players control a paddle to bounce a ball, destroy bricks, and progress through increasingly challenging levels with power-ups, enemies, and smooth scene transitions.

---

## ⚙️ Key Features
- 🧱 **Multiple Levels** with dynamic layouts and increasing difficulty  
- 💥 **Power-ups & Enemies** that modify gameplay and add strategy  
- 🎵 **Integrated Sound System** for background music and effects  
- 🎬 **Scene Transitions & Animations** for immersive flow  
- 💾 **Database-backed save system** (PostgreSQL / Supabase)  
- 🧠 **Pause, Resume, and Snapshot** features to restore gameplay seamlessly  
- 🧪 **TestFX UI Tests** and **JUnit backend tests** for reliability  

---

## 🧱 Project Architecture (MVC)

src/
├── main/
│ ├── java/com/game/arkanoid/
│ │ ├── app/ → Entry point (Main class)
│ │ ├── controller/ → FXML controllers for UI scenes
│ │ ├── services/ → Core game logic (GameService, PowerUpService, etc.)
│ │ ├── models/ → Entity classes (Ball, Paddle, Brick, etc.)
│ │ ├── events/ → Custom event system (GameEventBus)
│ │ ├── view/ → Rendering, sound, transition strategies
│ │ ├── repository/ → Database layer and SQL logic
│ │ ├── config/ → Database & environment configuration
│ │ └── utils/ → Shared constants and helpers
│ └── resources/com/game/arkanoid/
│ ├── fxml/ → JavaFX layout files
│ ├── css/ → Stylesheets
│ ├── images/ → Sprites and textures
│ ├── sounds/ → Audio resources
│ └── sql/ → Queries for game state, score, user
└── test/ → Unit & UI tests (JUnit + TestFX)


## 🧭 The project follows the **MVC pattern**:
- **Model:** Game state and entities  
- **View:** JavaFX visuals, renderers, and sound  
- **Controller:** Input handlers and scene management  

---

## 🧠 Object-Oriented Design Principles
| Principle         | Implementation                                                                           |
|-------------------|------------------------------------------------------------------------------------------|
| **Encapsulation** | Each entity (Ball, Paddle, Brick) maintains private state with controlled public access. |
| **Inheritance**   | MovableObject → Ball, Paddle, Bullet, Enemy share velocity logic.                        |
| **Polymorphism**  | Unified `update()` and `render()` interfaces for flexible behavior.                      |
| **Abstraction**   | Services (e.g., `GameService`, `EnemyService`) hide complex logic behind simple methods. |

---

## 🧰 Technologies Used
- ☕ **Java 25**  
- 🎨 **JavaFX 21** for GUI and animation  
- 🕹️ **FXGL Engine** for physics and game loops  
- 🧩 **PostgreSQL / Supabase** for persistent storage  
- ⚙️ **Dotenv** for environment configuration  
- 🧪 **JUnit 5 + TestFX** for automated testing  
- 🧱 **Maven** for dependency management and builds  

---

## 💾 Database Integration
The project integrates **PostgreSQL** using `DatabaseService`, handling:
- User login and signup  
- Scoreboard and ranking retrieval  
- Auto-saving game snapshots  
- Restoring “continue” state on startup  

🔐 Credentials are loaded via `.env` configuration.

---

## 🧪 Testing Strategy
- **Unit Tests:** Validate core logic (services, models, repositories)  
- **UI Tests:** `TestFX` automates FXML scene rendering and event testing  

Run all tests:
```bash
mvn clean test
```

## 🚀 How to Run

Make sure you have Java 24+ and Maven installed.
```bash
# Clone and build
git clone https://github.com/yourusername/arkanoid.git
cd arkanoid
mvn clean install

# Run the game
mvn javafx:run
```

Or directly:
```bash
mvn exec:java -Dexec.mainClass="com.game.arkanoid.app.Main"
```

## 🧠 UML & Architecture

Visual representation of the core architecture and class relationships:

 ![Project Architecture](docs/architecture.svg)
 
## 🗄️ Database Schema

The game persists user profiles and in‑progress game snapshots:

- Users (`users`)
  - `id` (PK), `name` (unique), `password`, `best_score`, `best_round`, `last_login`
- Game States (`game_states`)
  - Core: `user_id` (FK → users.id), `current_level`, `score`, `lives`, `paddle_x`, `ball_x`, `ball_y`
  - Rehydrate hints: `paddle_width`, `ball_dx`, `ball_dy`, `ball_moving`, `ball_downward`, `ball_stuck`, `ball_stuck_offset_x`, `time_scale`, `laser_cooldown`
  - Snapshot payloads (JSONB): `bricks`, `powerups`, `enemies`, `balls`, `effects`
  - Housekeeping: `in_progress`, `updated_at`

Rankings are derived directly from `users` (best score/round), while the latest in‑progress session is loaded from `game_states` per user.

![Database Schema](docs/database-schema.png)

## 📺 Gameplay Demo

🎥 Watch the gameplay demo on YouTube:
👉 https://youtu.be/your-demo-link

## Authors

| Name                 | Role                                             |
| -------------------- | -------------------------------------------------|
| **Nguyễn Văn TIến**  | Leader Developer / Game logic / Event System     |   
| **Nguyễn Bảo Minh**  | Database / System Architect / Scene Management   |
| **Lê Đình Anh Tuấn** | JavaFX- FXML / UI Intergration                   | 

## 🪪 License

This project was developed for academic and demonstration purposes.  
It is **not covered by an open-source license**. Redistribution or commercial use requires prior permission from the authors.


---

## 💭 Motivation & Inspiration
This project was inspired by the timeless classic **Arkanoid**, reimagined with modern Java technologies.  
Some visual assets and design ideas were adapted from open-source community projects.  
We would like to express our gratitude to the developers who shared their work publicly, which helped shape and inspire this project.


---
