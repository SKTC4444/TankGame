package TankGame.game;

import TankGame.GameConstants;
import TankGame.game.Audio;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Airstrike {
    private List<Explosion> explosions;
    private List<BufferedImage> frames;
    private long frameDuration;
    private boolean active;
    private Random random;

    public Airstrike(String gifPath, long frameDuration) {
        this.frames = loadGifFrames(gifPath, 100, 100);
        this.frameDuration = frameDuration;
        this.explosions = new CopyOnWriteArrayList<>();
        this.active = false;
        this.random = new Random();
    }

    private List<BufferedImage> loadGifFrames(String path, int targetWidth, int targetHeight) {
        List<BufferedImage> frames = new CopyOnWriteArrayList<>();
        try {
            ImageInputStream stream = ImageIO.createImageInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path), "GIF file not found"));
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(stream);
                int numFrames = reader.getNumImages(true);
                for (int i = 0; i < numFrames; i++) {
                    BufferedImage frame = reader.read(i);
                    frames.add(resizeImage(frame, targetWidth / 2, targetHeight / 2));
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

    public void start(float x, float y, float radius, int numExplosions, long duration) {
        explosions.clear();
        this.active = true;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numExplosions; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float explosionX = x + (float) (random.nextDouble() * radius * Math.cos(angle));
            float explosionY = y + (float) (random.nextDouble() * radius * Math.sin(angle));
            long explosionTime = startTime + (i * duration / numExplosions);
            explosions.add(new Explosion(explosionX, explosionY, explosionTime));
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Audio.playSound("tankgame-res/jet.wav");
            }
        }, 2000);
    }

    public void update(List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones, List<Mine> mines) {
        if (!active) return;
        long currentTime = System.currentTimeMillis();
        List<Explosion> toRemove = new ArrayList<>();
        for (Explosion explosion : explosions) {
            if ((currentTime - explosion.startTime) >= frameDuration * frames.size()) {
                toRemove.add(explosion);
            } else {
                applySplashDamage(explosion.x, explosion.y, breakableWalls, otherTank, drones, mines);
            }
        }
        explosions.removeAll(toRemove);
        if (explosions.isEmpty()) {
            active = false;
        }
    }

    private void applySplashDamage(float x, float y, List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones, List<Mine> mines) {
        if (frames.isEmpty()) {
            return;
        }
        BufferedImage explosionFrame = frames.get(3);
        int explosionWidth = explosionFrame.getWidth();
        int explosionHeight = explosionFrame.getHeight();
        Rectangle2D.Float explosionArea = new Rectangle2D.Float(x - explosionWidth / 2, y - explosionHeight / 2, explosionWidth, explosionHeight);

        if (explosionArea.intersects(otherTank.getBounds())) {
            otherTank.reduceHealth(300);
        }

        for (BreakableWall wall : breakableWalls) {
            if (explosionArea.intersects(wall.getBounds())) {
                wall.destroy();
            }
        }

        for (Drone drone : drones) {
            if (explosionArea.intersects(drone.getBounds())) {
                drone.explode();
            }
        }

        for (Mine mine : mines) {
            Rectangle mineBounds = mine.getBounds();
            if (explosionArea.intersects(mineBounds)) {
                mine.explode();
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (!active) return;
        long currentTime = System.currentTimeMillis();
        for (Explosion explosion : explosions) {
            if (currentTime >= explosion.startTime) {
                int currentFrame = (int) ((currentTime - explosion.startTime) / frameDuration);
                if (currentFrame < frames.size()) {
                    g2.drawImage(frames.get(currentFrame), (int) explosion.x, (int) explosion.y, null);
                }
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    private static class Explosion {
        float x, y;
        long startTime;

        Explosion(float x, float y, long startTime) {
            this.x = x;
            this.y = y;
            this.startTime = startTime;
        }
    }
}
