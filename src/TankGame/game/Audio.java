package TankGame.game;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Audio implements Runnable {
    private float volume;
    private Clip clip;
    private final String filePath;

    public Audio(String filePath) {
        this.filePath = filePath;
        this.volume = 0.0f;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volume);
        }
    }

    @Override
    public void run() {
        try {
            playMusic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMusic() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             InputStream bufferedIn = new BufferedInputStream(is);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn)) {
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            setVolume(volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null && clip.isRunning()) {
            return;
        }
        new Thread(this).start();
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public static void playSound(String filePath) {
        playSound(filePath, 0.0f);
    }

    public static void playSound(String filePath, float volume) {
        try (InputStream is = Audio.class.getClassLoader().getResourceAsStream(filePath);
             InputStream bufferedIn = new BufferedInputStream(is);
             AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volume);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
