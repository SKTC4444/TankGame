package TankGame.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Animation {
    private List<BufferedImage> frames;
    private int currentFrameIndex;
    private long lastFrameTime;
    private final int frameDuration;
    private boolean isActive;

    public Animation(String basePath, String filePrefix, int frameCount, int frameDuration) {
        frames = new ArrayList<>();
        this.frameDuration = frameDuration;
        for (int i = 1; i <= frameCount; i++) {
            try {
                frames.add(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        basePath + "/" + filePrefix + String.format("%04d", i) + ".png"), "Image file not found")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentFrameIndex = 0;
        lastFrameTime = System.currentTimeMillis();
        isActive = false;
    }

    public void update() {
        if (!isActive) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= frameDuration) {
            currentFrameIndex++;
            if (currentFrameIndex >= frames.size()) {
                isActive = false;
            }
            lastFrameTime = currentTime;
        }
    }

    public void draw(Graphics2D g, int x, int y) {
        if (!isActive || frames.isEmpty()) return;
        g.drawImage(frames.get(currentFrameIndex), x, y, null);
    }

    public void start() {
        isActive = true;
        currentFrameIndex = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    public boolean isFinished() {
        return !isActive;
    }

    public int getFrameWidth() {
        return frames.get(0).getWidth();
    }

    public int getFrameHeight() {
        return frames.get(0).getHeight();
    }
}
