package com.game.arkanoid.view.sound;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.*;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.*;

public final class SoundManager {
    private static SoundManager instance;
    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final Map<String, Media> soundMap = new HashMap<>();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();
    private final List<MediaPlayer> activePlayers = new ArrayList<>();
    private final DoubleProperty masterVolume = new SimpleDoubleProperty(1.0);
    private final DoubleProperty musicVolume  = new SimpleDoubleProperty(0.7);
    private final DoubleProperty sfxVolume    = new SimpleDoubleProperty(0.8);
    private MediaPlayer bgmPlayer;

    private SoundManager() {
        loadSounds();
        registerEventListeners();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void registerEventListeners() {
        subscriptions.add(eventBus.subscribe(GameBGMSoundEvent.class, e -> playGameBGM()));
        subscriptions.add(eventBus.subscribe(MenuBGMSoundEvent.class, e -> playMenuBGM()));
        subscriptions.add(eventBus.subscribe(RoundStartSoundEvent.class, e -> playRoundStart()));
        subscriptions.add(eventBus.subscribe(BrickHitSoundEvent.class, e -> playBrickHit()));
        subscriptions.add(eventBus.subscribe(PaddleHitSoundEvent.class, e -> playPaddleHit()));
        subscriptions.add(eventBus.subscribe(WallHitSoundEvent.class, e -> playWallHit()));
        subscriptions.add(eventBus.subscribe(StopBGMSoundEvent.class, e -> stopBGM()));
        subscriptions.add(eventBus.subscribe(PowerUpHitSoundEvent.class, e -> playPowerUpHit()));
        subscriptions.add(eventBus.subscribe(BulletFireSoundEvent.class, e -> playBulletFire()));
    }

    private void loadSounds() {
        loadSound("menu", "/com/game/arkanoid/sounds/menu_sound.mp3");
        loadSound("round_start", "/com/game/arkanoid/sounds/round_start.mp3");
        loadSound("game", "/com/game/arkanoid/sounds/game_sound.mp3");
        loadSound("brick_hit", "/com/game/arkanoid/sounds/brick_hit.mp3");
        loadSound("paddle_hit", "/com/game/arkanoid/sounds/paddle_hit.mp3");
        loadSound("wall_hit", "/com/game/arkanoid/sounds/wall_hit.mp3");
        loadSound("bullet_fire", "/com/game/arkanoid/sounds/bullet_fire.mp3");
        loadSound("game_over", "/com/game/arkanoid/sounds/game_over.mp3");
        loadSound("powerup_hit", "/com/game/arkanoid/sounds/powerup_hit.mp3");
    }

    private void loadSound(String key, String path) {
        try {
            Media media = new Media(getClass().getResource(path).toExternalForm());
            soundMap.put(key, media);
        } catch (Exception e) {
            System.err.println("Cannot load sound: " + path + " (" + e.getMessage() + ")");
        }
    }

    // --- SFX ---
    private void play(String key) {
        Media media = soundMap.get(key);
        if (media == null) return;

        MediaPlayer player = new MediaPlayer(media);
        activePlayers.add(player);

        // Bind volume: master * sfx
        player.volumeProperty().bind(sfxVolume.multiply(masterVolume));

        player.setOnEndOfMedia(() -> {
            player.dispose();
            activePlayers.remove(player);
        });

        player.play();
    }

    // --- BGM ---
    private void playLoop(String key) {
        Media media = soundMap.get(key);
        if (media == null) return;

        stopLoop();
        bgmPlayer = new MediaPlayer(media);

        // Bind volume: master * music
        bgmPlayer.volumeProperty().bind(musicVolume.multiply(masterVolume));

        bgmPlayer.setOnEndOfMedia(() -> bgmPlayer.seek(Duration.ZERO));
        bgmPlayer.play();
    }

    public void playMenuBGM() { playLoop("menu"); }
    public void playGameBGM() { playLoop("game"); }
    public void stopBGM() { stopLoop(); }
    public void playRoundStart() { play("round_start"); }
    public void playBrickHit() { play("brick_hit"); }
    public void playPaddleHit() { play("paddle_hit"); }
    public void playWallHit() { play("wall_hit"); }
    public void playBulletFire() { play("bullet_fire"); }
    public void playGameOver() { play("game_over"); }
    public void playPowerUpHit() { play("powerup_hit"); }

    private void stopLoop() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    public DoubleProperty masterVolumeProperty() { return masterVolume; }
    public DoubleProperty musicVolumeProperty()  { return musicVolume; }
    public DoubleProperty sfxVolumeProperty()    { return sfxVolume; }

    public void dispose() {
        stopLoop();
        soundMap.clear();
        subscriptions.forEach(GameEventBus.Subscription::close);
        subscriptions.clear();
    }
}
