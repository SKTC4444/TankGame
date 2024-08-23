package TankGame.game;

import TankGame.GameConstants;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class Round {
    private float x, y;
    private float angle;
    private final float speed = 11.0f;
    private boolean active = true;
    private static final int DAMAGE = 2;
    public static final int TARGET_WIDTH = 30;
    public static final int TARGET_HEIGHT = 30;

    private BufferedImage img;

    public Round(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;

        try {
            BufferedImage originalImg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("tankgame-res/bullet.png"));
            if (originalImg != null) {
                this.img = resizeImage(originalImg, TARGET_WIDTH, TARGET_HEIGHT);
            } else {
                System.out.println("Error: Bullet image not found.");
            }
        } catch (IOException e) {
            System.out.println("Error loading round image: " + e);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    public void update(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank, List<Mine> mines, List<Drone> drones) {
        if (active) {
            x += speed * Math.cos(Math.toRadians(angle));
            y += speed * Math.sin(Math.toRadians(angle));

            if (x < 0 || x > GameConstants.GAME_WORLD_WIDTH || y < 0 || y > GameConstants.GAME_WORLD_HEIGHT) {
                active = false;
                return;
            }

            if (getBounds().intersects(otherTank.getBounds())) {
                active = false;
                otherTank.reduceHealth(DAMAGE);
                return;
            }

            for (UnbreakableWall wall : unbreakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    active = false;
                    return;
                }
            }

            for (BreakableWall wall : breakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    active = false;
                    return;
                }
            }

            for (Mine mine : mines) {
                if (getBounds().intersects(mine.getBounds()) && mine.isActive()) {
                    active = false;
                    mine.explode();
                    return;
                }
            }

            for (Drone drone : drones) {
                if (getBounds().intersects(drone.getBounds()) && drone.isActive()) {
                    active = false;
                    drone.explode();
                    return;
                }
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle2D.Float getBounds() {
        return new Rectangle2D.Float(x, y, img != null ? img.getWidth() : 0, img != null ? img.getHeight() : 0);
    }

    public void draw(Graphics2D g2) {
        if (active && img != null) {
            AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
            rotation.rotate(Math.toRadians(angle), img.getWidth() / 2.0, img.getHeight() / 2.0);
            g2.drawImage(img, rotation, null);
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
