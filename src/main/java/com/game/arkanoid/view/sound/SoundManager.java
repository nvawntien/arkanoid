package com.game.arkanoid.view.sound;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.*;

public final class SoundManager {
    private static SoundManager instance;

    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final Map<String, Media> bgmMap = new HashMap<>();
    private final Map<String, AudioClip> sfxMap = new HashMap<>();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();
    private MediaPlayer bgmPlayer;

    private final DoubleProperty masterVolume = new SimpleDoubleProperty(1.0);
    private final DoubleProperty musicVolume = new SimpleDoubleProperty(0.7);
    private final DoubleProperty sfxVolume = new SimpleDoubleProperty(0.8);

    private final ExecutorService soundExecutor = Executors.newCachedThreadPool();

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

    private void loadSounds() {
        // BGM
        loadBGM("menu", "/com/game/arkanoid/sounds/menu_sound.mp3");
        loadBGM("game", "/com/game/arkanoid/sounds/game_sound.mp3");
        loadBGM("round_start", "/com/game/arkanoid/sounds/round_start.mp3");

        // SFX
        loadSFX("brick_hit", "/com/game/arkanoid/sounds/brick_hit.wav");
        loadSFX("paddle_hit", "/com/game/arkanoid/sounds/paddle_hit.wav");
        loadSFX("wall_hit", "/com/game/arkanoid/sounds/wall_hit.wav");
        loadSFX("bullet_fire", "/com/game/arkanoid/sounds/bullet_fire.wav");
        loadSFX("powerup_hit", "/com/game/arkanoid/sounds/powerup_hit.wav");
        loadSFX("game_over", "/com/game/arkanoid/sounds/game_over.wav");
    }

    private void loadBGM(String key, String path) {
        try {
            Media media = new Media(getClass().getResource(path).toExternalForm());
            bgmMap.put(key, media);
        } catch (Exception e) {
            System.err.println("Cannot load BGM: " + path + " (" + e.getMessage() + ")");
        }
    }

    private void loadSFX(String key, String path) {
        try {
            AudioClip clip = new AudioClip(getClass().getResource(path).toExternalForm());
            sfxMap.put(key, clip);
        } catch (Exception e) {
            System.err.println("Cannot load SFX: " + path + " (" + e.getMessage() + ")");
        }
    }

    private void registerEventListeners() {
        subscriptions.add(eventBus.subscribe(GameBGMSoundEvent.class, e -> playBGM("game")));
        subscriptions.add(eventBus.subscribe(MenuBGMSoundEvent.class, e -> playBGM("menu")));
        subscriptions.add(eventBus.subscribe(RoundStartSoundEvent.class, e -> playBGM("round_start")));
        subscriptions.add(eventBus.subscribe(StopBGMSoundEvent.class, e -> stopBGM()));

        subscriptions.add(eventBus.subscribe(BrickHitSoundEvent.class, e -> playSFX("brick_hit")));
        subscriptions.add(eventBus.subscribe(PaddleHitSoundEvent.class, e -> playSFX("paddle_hit")));
        subscriptions.add(eventBus.subscribe(WallHitSoundEvent.class, e -> playSFX("wall_hit")));
        subscriptions.add(eventBus.subscribe(BulletFireSoundEvent.class, e -> playSFX("bullet_fire")));
        subscriptions.add(eventBus.subscribe(PowerUpHitSoundEvent.class, e -> playSFX("powerup_hit")));
        subscriptions.add(eventBus.subscribe(GameOverSoundEvent.class, e -> playSFX("game_over")));
    }

    /** --------------------------
     *  Play BGM
     *  -------------------------- */
    private void playBGM(String key) {
        soundExecutor.submit(() -> {
            stopBGM();
            Media media = bgmMap.get(key);
            if (media == null) return;

            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.volumeProperty().bind(masterVolume.multiply(musicVolume));
            bgmPlayer.play();
        });
    }

    private void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    /** --------------------------
     *  Play SFX
     *  -------------------------- */
    private void playSFX(String key) {
        AudioClip clip = sfxMap.get(key);
        if (clip == null) return;

        soundExecutor.submit(() -> clip.play(masterVolume.get() * sfxVolume.get()));
    }

    /** --------------------------
     *  Volume properties
     *  -------------------------- */
    public DoubleProperty masterVolumeProperty() { return masterVolume; }
    public DoubleProperty musicVolumeProperty() { return musicVolume; }
    public DoubleProperty sfxVolumeProperty() { return sfxVolume; }

    /** --------------------------
     *  Cleanup
     *  -------------------------- */
    public void dispose() {
        stopBGM();
        subscriptions.forEach(GameEventBus.Subscription::close);
        subscriptions.clear();
        soundExecutor.shutdown();
    }
}