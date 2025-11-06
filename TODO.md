Refactor to Clean MVC + EventBus + Proper Scene Routing
Goals (non-negotiable)

Enforce clean MVC boundaries:

Services (domain/application logic): no JavaFX, no render/audio calls.

View (renderers, transitions, sound): only presentation concerns, no game rules.

Controllers (UI logic): scene flow, user input wiring, subscribe to events, call services.

Introduce an EventBus (Observer) for logic → view communication.

Move SceneNavigator to the correct layer and trim it to pure scene routing.

Preserve current behavior & performance; no regressions.

Current Structure (abridged):
com.game.arkanoid
├─ app, config, container
├─ controller
├─ models
├─ services
│  ├─ (BallService, BricksService, PaddleService, PowerUpService, RoundService, GameService)
│  └─ sound/SoundService         <-- presentation leakage in services
├─ utils
└─ view
   ├─ renderer/* (PaddleRenderer, BallRenderer, ...)
   ├─ transition/* (TransitionManager, strategies)
   ├─ SceneNavigator             <-- controller infra living in view

Target Structure (proposed)

com.game.arkanoid
├─ controller
│  ├─ GameController, MenuController, SettingsController, GameOverController
│  └─ infra/
│     └─ SceneNavigator.java    <-- MOVE HERE (controller infra; scene router only)
├─ events/
│  ├─ GameEventBus.java
│  ├─ BrickHitEvent.java
│  ├─ BrickDestroyedEvent.java
│  ├─ PaddleHitEvent.java
│  ├─ PowerUpSpawnedEvent.java
│  ├─ PowerUpCollectedEvent.java
│  ├─ PowerUpExpiredEvent.java
│  ├─ LifeLostEvent.java
│  ├─ GameOverEvent.java
│  └─ LevelClearedEvent.java
├─ models
├─ services                     <-- PURE logic/application; no JavaFX, no view deps
│  ├─ BallService.java
│  ├─ BricksService.java
│  ├─ PaddleService.java
│  ├─ PowerUpService.java
│  ├─ RoundService.java
│  └─ GameService.java          <-- orchestrator logic; publishes events only
├─ view
│  ├─ renderer/
│  ├─ transition/
│  └─ sound/
│     └─ SoundService.java      <-- MOVE HERE (presentation)
└─ utils
Core Refactors
1) Add EventBus (Observer)

 Create events/GameEventBus.java (simple pub/sub, type-safe).

API:

public static GameEventBus i()

<T> void subscribe(Class<T> type, Consumer<T> handler)

void publish(Object event)

 Add event POJOs (records recommended):

 BrickHitEvent

 BrickDestroyedEvent

 PaddleHitEvent

 PowerUpSpawnedEvent(PowerUpType type)

 PowerUpCollectedEvent(PowerUpType type)

 PowerUpExpiredEvent(PowerUpType type)

 LifeLostEvent

 GameOverEvent

 LevelClearedEvent(int level)

2) Decouple Services from View/Sound (strict MVC)

 PowerUpService

Remove params/fields of any view types (PaddleRenderer, SoundService).

API updates:

update(GameState state, double dt, double worldW, double worldH) (no renderer/sound)

applyPowerUp(...) updates model only.

On collect/expire/spawn: GameEventBus.i().publish(new PowerUpCollectedEvent(type)) etc.

 GameService

Remove direct calls to SoundService and any renderer.

Replace with GameEventBus publishes:

On paddle/brick hit, destroyed, life lost, game over, level cleared.

Keep orchestration of logic only: input handling, collisions, scoring, lives, round handoffs.

 RoundService

Remains pure logic: load/reset level, update GameState.

No transitions, no sound, no FXML, no scene changes.

 BallService / BricksService / PaddleService

Verify no JavaFX usage. Keep them pure domain/app logic.

3) Move and Slim SceneNavigator

 Move view/SceneNavigator.java → controller/infra/SceneNavigator.java.

 Ensure SceneNavigator only:

Loads FXML, constructs controllers via FXMLLoader.

Sets scene on Stage.

Invokes TransitionManager (which remains under view/transition).

Optionally notifies active GameController lifecycle (stop()).

 No game logic, no direct calls to services except DI wiring during controller creation.

4) Wire View Layer via EventBus

 In view/sound/SoundService:

Subscribe to: BrickHitEvent, BrickDestroyedEvent, PaddleHitEvent, PowerUpCollectedEvent, LifeLostEvent, GameOverEvent, LevelClearedEvent.

Play mapped SFX accordingly. (No changes to preload/fade APIs.)

 In renderers (e.g., PaddleRenderer):

Subscribe to PowerUpCollectedEvent(EXPAND_PADDLE) → playExpand(...).

Subscribe to PowerUpExpiredEvent(EXPAND_PADDLE) → playShrink(...).

 In controllers (GameController, RoundController if created):

Subscribe to LevelClearedEvent → trigger UI transition/overlay countdown then call roundSvc.loadNextLevel(state).

5) Container / DI Updates

 Container remains the composition root for services and state.

 Ensure no view code is instantiated inside service constructors.

 Controllers receive:

GameState, GameService, and SceneNavigator.

View singletons (SoundService) do not get injected into logic services.

Bugfix/Behavior Acceptance (related to refactor)

No stray power-up after level transition

Steps: Destroy last brick → countdown transition → next level.

Expect: No PowerUp from previous level falls after the countdown.

Mechanism:

RoundService.loadLevel() already state.powerUps.clear(); ensure any renderer nodes are not independently lingering (view subscribes to model updates only).

Ensure controllers stop per-scene timelines before switching scenes.

Animation coupling removed

PowerUpService no longer calls PaddleRenderer methods; instead, events drive animations.

Sound triggers removed from logic

GameService no longer calls soundSvc.playSfx; sound is event-driven.

SceneNavigator

Lives in controller/infra.

No direct references to domain logic beyond DI to controllers.

Transitions are invoked via TransitionManager.

Code Changes Checklist (surgical)

 PowerUpService

 Remove ctor SoundService soundService.

 update(...) signature: drop PaddleRenderer & BallService param; if needed, keep BallService only for catch/reset; prefer to call via GameService if orchestration grows.

 Replace paddleRenderer.playExpand/ playShrink with publish events.

 GameService

 Remove field SoundService soundSvc.

 Replace all soundSvc.playSfx("...") with event publishes:

new PaddleHitEvent(), new BrickHitEvent(), new BrickDestroyedEvent(),
new LifeLostEvent(), new GameOverEvent(), new LevelClearedEvent(state.level).

 update(...) signature: drop PaddleRenderer param.

 SceneNavigator

 Move to controller/infra/.

 Keep only FXML load, scene set, transitions; keep optional active game controller stop hook.

 SoundService

 Move to view/sound/.

 Subscribe to all relevant events; map to existing SFX ids.

 Renderers

 Subscribe to PowerUpCollectedEvent/ExpiredEvent for paddle expand/shrink visuals.

 Controllers

 Subscribe to LevelClearedEvent and run visual countdown/transition; call roundSvc.loadNextLevel(state) after transition finishes.

 | Event                      | Publisher            | Subscribers                                  | Action                                     |
| -------------------------- | -------------------- | -------------------------------------------- | ------------------------------------------ |
| `PaddleHitEvent`           | GameService          | SoundService                                 | `playSfx("paddle_hit")`                    |
| `BrickHitEvent`            | GameService          | SoundService                                 | `playSfx("brick_hit")`                     |
| `BrickDestroyedEvent`      | GameService          | SoundService, BricksRenderer (optional)      | `playSfx("brick_break")`, break anim       |
| `PowerUpSpawnedEvent(t)`   | GameService/PowerUpS | SoundService (optional)                      | `playSfx("powerup_spawn")`                 |
| `PowerUpCollectedEvent(t)` | PowerUpService       | SoundService, PaddleRenderer                 | `playSfx("powerup_collect")`, `playExpand` |
| `PowerUpExpiredEvent(t)`   | PowerUpService       | PaddleRenderer                               | `playShrink`                               |
| `LifeLostEvent`            | GameService          | SoundService, HUD (optional)                 | `playSfx("life_lost")`                     |
| `GameOverEvent`            | GameService          | SoundService, GameController                 | `playSfx("game_over")`, navigate to over   |
| `LevelClearedEvent(n)`     | GameService          | SoundService, GameController/RoundController | `playSfx("level_clear")`, transition/next  |

Testing / QA

 Unit: Services compile & run without JavaFX; EventBus unit tests for pub/sub.

 Integration: Playing through levels triggers correct sounds/animations via events.

 Regression: No power-ups or render nodes persist between scenes.

 Manual: Pause/resume works; transitions play; scene routing stable.

Constraints & Style

Keep package names and public APIs stable where possible.

No silent behavior change (only decouple responsibilities).

Prefer records for event payloads.

No view/presentation dependencies in services/*.


Nice-to-Have

 Add RoundController (optional) to own level transitions/countdown, leaving GameController focused on in-level UI.

 Convert SoundService naming to AudioManager (clearer presentation role).

 Add lightweight EventBus unsubscribe support for controller disposal.
