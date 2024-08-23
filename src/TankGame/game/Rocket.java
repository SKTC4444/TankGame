package TankGame.game;

import TankGame.GameConstants;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Rocket {
    private float x, y;
    private float angle;
    private BufferedImage img;
    private final float speed = 7.0f;
    private boolean active = true;
    private List<BufferedImage> impactFrames;
    private int impactFrameIndex;
    private boolean hasImpacted = false;
    private Audio rocketSound;
    private long lastImpactFrameTime;
    private static final int IMPACT_FRAME_DURATION = 100;
    private boolean splashDamageApplied = false;


    public Rocket(float x, float y, float angle, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.img = img;
        this.impactFrames = loadGifFrames("tankgame-res/explosionmassive.gif", img.getWidth() * 7, img.getHeight() * 7);
        this.impactFrameIndex = 0;
        this.lastImpactFrameTime = 0;
        this.rocketSound = new Audio("tankgame-res/rocketlaunch.wav");
        this.rocketSound.setVolume(-3.0f);
        this.rocketSound.play();
    }

    private List<BufferedImage> loadGifFrames(String path, int width, int height) {
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
                    frames.add(resizeImage(frame, width, height));
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

    public void update(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones, List<Mine> mines) {
        if (active) {
            x += speed * Math.cos(Math.toRadians(angle));
            y += speed * Math.sin(Math.toRadians(angle));

            if (x < 0 || x > GameConstants.GAME_WORLD_WIDTH || y < 0 || y > GameConstants.GAME_WORLD_HEIGHT) {
                deactivate("Out of bounds", breakableWalls, otherTank, drones, mines);
                return;
            }

            if (getBounds().intersects(otherTank.getBounds())) {
                deactivate("Hit tank", breakableWalls, otherTank, drones, mines);
                return;
            }

            for (UnbreakableWall wall : unbreakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    deactivate("Hit unbreakable wall", breakableWalls, otherTank, drones, mines);
                    return;
                }
            }

            for (BreakableWall wall : breakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    wall.destroy();
                    deactivate("Hit breakable wall", breakableWalls, otherTank, drones, mines);
                    return;
                }
            }

            for (Drone drone : drones) {
                if (getBounds().intersects(drone.getBounds())) {
                    drone.explode();
                    deactivate("Hit drone", breakableWalls, otherTank, drones, mines);
                    return;
                }
            }
        } else if (hasImpacted) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastImpactFrameTime >= IMPACT_FRAME_DURATION) {
                impactFrameIndex++;
                if (impactFrameIndex >= impactFrames.size()) {
                    impactFrameIndex = 0;
                    hasImpacted = false;
                }
                lastImpactFrameTime = currentTime;
            }
        }
    }

    public void deactivate(String reason, List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones, List<Mine> mines) {
        active = false;
        hasImpacted = true;
        if (rocketSound != null) {
            rocketSound.stop();
        }
        Audio.playSound("tankgame-res/massiveexplosion.wav");
        lastImpactFrameTime = System.currentTimeMillis();
        if (!splashDamageApplied) {
            applySplashDamage(breakableWalls, otherTank, drones, mines);
            splashDamageApplied = true;
        }
    }


    private void applySplashDamage(List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones, List<Mine> mines) {
        if (impactFrames.size() < 5) {
            return;
        }
        BufferedImage fifthFrame = impactFrames.get(5);
        int explosionWidth = fifthFrame.getWidth();
        int explosionHeight = fifthFrame.getHeight();
        Rectangle2D.Float explosionArea = new Rectangle2D.Float(x - explosionWidth / 2, y - explosionHeight / 2, explosionWidth, explosionHeight);

        if (explosionArea.intersects(otherTank.getBounds())) {
            otherTank.reduceHealth(200);
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

    public boolean isActive() {
        return active || hasImpacted;
    }

    public Rectangle2D.Float getBounds() {
        return new Rectangle2D.Float(x, y, img.getWidth(), img.getHeight());
    }

    public void draw(Graphics2D g2) {
        if (active) {
            AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
            rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
            g2.drawImage(this.img, rotation, null);
        } else if (hasImpacted && impactFrameIndex < impactFrames.size()) {
            BufferedImage impactFrame = impactFrames.get(impactFrameIndex);
            int drawX = (int) (x - impactFrame.getWidth() / 2.0 + img.getWidth() / 2.0);
            int drawY = (int) (y - impactFrame.getHeight() / 2.0 + img.getHeight() / 2.0);
            g2.drawImage(impactFrame, drawX, drawY, null);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAngle() {
        return angle;
    }

    public BufferedImage getImage() {
        return img;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
