package TankGame.game;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StarEffect {
    private boolean active;
    private long startTime;
    private static final int DURATION = 7000;
    private float currentHueShift = 0.0f;
    private List<BufferedImage> sparkleFrames;
    private int sparkleFrameIndex;
    private long lastSparkleFrameTime;
    private static final int SPARKLE_FRAME_DURATION = 50;
    private static Clip starClip;
    private final Audio gameMusic;
    private static int activeStarEffects = 0;

    public StarEffect(Audio gameMusic) {
        this.active = false;
        this.sparkleFrames = loadGifFrames("tankgame-res/rainbowsparkles.gif");
        this.sparkleFrameIndex = 0;
        this.lastSparkleFrameTime = 0;
        this.gameMusic = gameMusic;
        this.starClip = loadAudioClip("tankgame-res/star.wav");
    }

    public void activate() {
        this.active = true;
        this.startTime = System.currentTimeMillis();
        gameMusic.setVolume(-20.0f);
        activeStarEffects++;
        playStarSound();
    }

    public void update() {
        if (active && System.currentTimeMillis() - startTime >= DURATION) {
            this.active = false;
            activeStarEffects--;
            if (activeStarEffects == 0) {
                gameMusic.setVolume(-9.0f);
                stopStarSound();
            }
        }
        if (active) {
            currentHueShift += 0.01f;
            if (currentHueShift > 1.0f) {
                currentHueShift = 0.0f;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSparkleFrameTime >= SPARKLE_FRAME_DURATION) {
                sparkleFrameIndex = (sparkleFrameIndex + 1) % sparkleFrames.size();
                lastSparkleFrameTime = currentTime;
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public BufferedImage applyEffect(BufferedImage img) {
        if (!active) return img;

        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());
        float[] hsbvals = new float[3];

        float hueShift = currentHueShift;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                Color.RGBtoHSB(r, g, b, hsbvals);

                float hue = hsbvals[0] + hueShift;
                if (hue > 1.0f) hue -= 1.0f;
                if (hue < 0.0f) hue += 1.0f;

                float saturation = hsbvals[1] * 1.5f;
                if (saturation > 1.0f) saturation = 1.0f;
                if (saturation < 0.0f) saturation = 0.0f;

                int newRGB = Color.HSBtoRGB(hue, saturation, hsbvals[2]);
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    public void drawSparkles(Graphics2D g2, int x, int y, int width, int height) {
        if (active && !sparkleFrames.isEmpty()) {
            BufferedImage sparkleFrame = sparkleFrames.get(sparkleFrameIndex);
            g2.drawImage(sparkleFrame, x, y, width, height, null);
        }
    }

    private List<BufferedImage> loadGifFrames(String path) {
        List<BufferedImage> frames = new ArrayList<>();
        try {
            ImageInputStream stream = ImageIO.createImageInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path), "GIF file not found"));
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                int numFrames = reader.getNumImages(true);
                for (int i = 0; i < numFrames; i++) {
                    BufferedImage frame = reader.read(i);
                    frames.add(frame);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load GIF frames: " + e.getMessage());
        }
        return frames;
    }

    private Clip loadAudioClip(String filePath) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
            if (is == null) {
                throw new IOException("Audio file not found: " + filePath);
            }

            try (InputStream bufferedIn = new BufferedInputStream(is);
                 AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                return clip;
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void playStarSound() {
        if (starClip != null && !starClip.isRunning()) {
            starClip.setFramePosition(0);
            starClip.start();
        }
    }

    public static void stopStarSound() {
        if (starClip != null) {
            starClip.stop();
        }
    }
}
