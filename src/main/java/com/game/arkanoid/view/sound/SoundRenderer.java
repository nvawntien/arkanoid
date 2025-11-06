package com.game.arkanoid.view.sound;

import com.game.arkanoid.config.GameSettings;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Presentation-layer audio manager used by controllers/services to trigger SFX/BGM directly.
 */
public final class SoundService {

    private static final String SOUND_ROOT = "/com/game/arkanoid/sounds/";

    private static final Map<String, String> SFX_FILES = Map.ofEntries(
            Map.entry("paddle_hit", "ball_paddle.wav"),
            Map.entry("brick_hit", "ball_block.wav"),
            Map.entry("brick_break", "explosion.wav"),
            Map.entry("powerup_spawn", "level_ready.wav"),
            Map.entry("powerup_collect", "level_ready.wav"),
            Map.entry("life_lost", "game_over.wav"),
            Map.entry("game_over", "game_over.wav"),
            Map.entry("level_clear", "level_ready.wav"),
            Map.entry("menu_click", "game_start.wav"),
            Map.entry("pause_on", "level_ready.wav"),
            Map.entry("pause_off", "game_start.wav")
    );

    private static final Map<String, String> BGM_FILES = Map.ofEntries(
            Map.entry("menu_bgm", "game_start.wav"),
            Map.entry("level_bgm", "ball_hard_block.wav")
    );

    private static SoundService instance;

    private final Map<String, AudioClip> sfxClips = new HashMap<>();
    private final Map<String, MediaPlayer> bgmPlayers = new HashMap<>();
    private final Map<String, Timeline> fadeTimelines = new HashMap<>();

    private final DoubleProperty masterVolume = new SimpleDoubleProperty(1.0);
    private final DoubleProperty musicVolume = new SimpleDoubleProperty(0.7);
    private final DoubleProperty sfxVolume = new SimpleDoubleProperty(0.8);

    private SoundService() {
        preloadSfx();
        preloadBgm();
        bindSettingDefaults();
        setupVolumePropagation();
    }

    public static synchronized SoundService getInstance() {
        if (instance == null) {
            instance = new SoundService();
        }
        return instance;
    }

    private void preloadSfx() {
        SFX_FILES.forEach((id, file) -> {
            URL url = getResource(SOUND_ROOT + file);
            if (url == null) {
                System.err.println("[SoundService] Missing SFX resource for id " + id + ": " + file);
                return;
            }
            AudioClip clip = new AudioClip(url.toExternalForm());
            clip.setCycleCount(1);
            sfxClips.put(id, clip);
        });
    }

    private void preloadBgm() {
        BGM_FILES.forEach((id, file) -> {
            URL url = getResource(SOUND_ROOT + file);
            if (url == null) {
                System.err.println("[SoundService] Missing BGM resource for id " + id + ": " + file);
                return;
            }
            Media media = new Media(url.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayers.put(id, player);
        });
    }

    private void bindSettingDefaults() {
        masterVolume.set(GameSettings.getMasterVolume());
        musicVolume.set(GameSettings.getMusicVolume());
        sfxVolume.set(GameSettings.getSfxVolume());
        masterVolume.addListener((obs, oldV, newV) -> GameSettings.setMasterVolume(newV.doubleValue()));
        musicVolume.addListener((obs, oldV, newV) -> GameSettings.setMusicVolume(newV.doubleValue()));
        sfxVolume.addListener((obs, oldV, newV) -> GameSettings.setSfxVolume(newV.doubleValue()));
    }

    private void setupVolumePropagation() {
        masterVolume.addListener((obs, oldV, newV) -> updateAllVolumes());
        musicVolume.addListener((obs, oldV, newV) -> updateAllVolumes());
        sfxVolume.addListener((obs, oldV, newV) -> updateAllVolumes());
        updateAllVolumes();
    }

    private void updateAllVolumes() {
        double musicGain = clamp(masterVolume.get() * musicVolume.get());
        double sfxGain = clamp(masterVolume.get() * sfxVolume.get());
        bgmPlayers.values().forEach(player -> player.setVolume(musicGain));
        sfxClips.values().forEach(clip -> clip.setVolume(sfxGain));
    }

    public double effectiveMusicVolume() {
        return clamp(masterVolume.get() * musicVolume.get());
    }

    public DoubleProperty masterVolumeProperty() {
        return masterVolume;
    }

    public DoubleProperty musicVolumeProperty() {
        return musicVolume;
    }

    public DoubleProperty sfxVolumeProperty() {
        return sfxVolume;
    }

    public void playSfx(String id) {
        AudioClip clip = sfxClips.get(id);
        if (clip == null) {
            System.err.println("[SoundService] Unknown SFX id: " + id);
            return;
        }
        double gain = clamp(masterVolume.get() * sfxVolume.get());
        clip.setVolume(gain);
        Platform.runLater(clip::play);
    }

    public void loopBgm(String id) {
        MediaPlayer player = bgmPlayers.get(id);
        if (player == null) {
            System.err.println("[SoundService] Unknown BGM id: " + id);
            return;
        }
        double gain = clamp(masterVolume.get() * musicVolume.get());
        Platform.runLater(() -> {
            player.stop();
            player.setVolume(gain);
            player.play();
        });
    }

    public void stopBgm(String id) {
        MediaPlayer player = bgmPlayers.get(id);
        if (player == null) {
            return;
        }
        Platform.runLater(player::stop);
    }

    public void fade(String id, double targetVolume) {
        fade(id, targetVolume, Duration.millis(400));
    }

    public void fade(String id, double targetVolume, Duration duration) {
        MediaPlayer player = bgmPlayers.get(id);
        if (player == null) {
            return;
        }
        double clampedTarget = clamp(targetVolume);
        Platform.runLater(() -> {
            Timeline existing = fadeTimelines.remove(id);
            if (existing != null) {
                existing.stop();
            }
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, evt -> { }),
                    new KeyFrame(duration, evt -> player.setVolume(clampedTarget))
            );
            timeline.setOnFinished(evt -> fadeTimelines.remove(id));
            fadeTimelines.put(id, timeline);
            timeline.play();
        });
    }

    public void stopAll() {
        Platform.runLater(() -> bgmPlayers.values().forEach(MediaPlayer::stop));
    }

    private URL getResource(String path) {
        return SoundService.class.getResource(path);
    }

    private double clamp(double v) {
        if (Double.isNaN(v)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, v));
    }
}
