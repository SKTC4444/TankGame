package TankGame.game;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ItemBox {
    private float x, y;
    private List<BufferedImage> frames;
    private boolean active;
    private int currentFrame;
    private long lastFrameChangeTime;
    private static final int FRAME_CHANGE_DELAY = 50;
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    public ItemBox(float x, float y, String gifPath) {
        this.x = x;
        this.y = y;
        this.active = true;
        this.frames = loadGifFrames(gifPath);
        this.currentFrame = 0;
        this.lastFrameChangeTime = System.currentTimeMillis();
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
                    frames.add(resizeImage(frame, WIDTH, HEIGHT));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load GIF frames: " + e.getMessage());
        }
        return frames;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Rectangle getBounds() {
        if (!frames.isEmpty()) {
            BufferedImage img = frames.get(0);
            return new Rectangle((int) x, (int) y, img.getWidth(), img.getHeight());
        }
        return new Rectangle((int) x, (int) y, WIDTH, HEIGHT);
    }

    public void draw(Graphics2D g2) {
        if (active && !frames.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameChangeTime >= FRAME_CHANGE_DELAY) {
                currentFrame = (currentFrame + 1) % frames.size();
                lastFrameChangeTime = currentTime;
            }
            g2.drawImage(frames.get(currentFrame), (int) x, (int) y, null);
        }
    }

    public void respawn(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        this.active = true;
    }
}
