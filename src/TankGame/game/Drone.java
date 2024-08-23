package TankGame.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Drone {
    private float x, y;
    private float vx, vy;
    private float angle;
    private BufferedImage img;
    private Tank targetTank;
    private boolean active;
    private Animation impactAnimation;
    private boolean hasImpacted = false;
    private Audio droneSound;

    public Drone(float x, float y, float angle, Tank targetTank) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.targetTank = targetTank;
        this.active = true;
        this.impactAnimation = new Animation("tankgame-res/explosion_lg", "explosion_lg_", 6, 50);
        this.droneSound = new Audio("tankgame-res/drone.wav");
        this.droneSound.setVolume(5.0f);

        try {
            BufferedImage originalImg = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/drone.png"), "Drone image not found"));
            this.img = resizeImage(originalImg, originalImg.getWidth() / 5, originalImg.getHeight() / 5);
        } catch (IOException e) {
            System.err.println("Error loading drone image: " + e.getMessage());
        }
        droneSound.play();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    public boolean isActive() {
        return active || !impactAnimation.isFinished();
    }

    public void update() {
        if (active) {
            float targetX = targetTank.getX();
            float targetY = targetTank.getY();
            angle = (float) Math.toDegrees(Math.atan2(targetY - y, targetX - x));

            vx = (float) Math.cos(Math.toRadians(angle)) * 3;
            vy = (float) Math.sin(Math.toRadians(angle)) * 3;
            x += vx;
            y += vy;

            if (Math.hypot(targetX - x, targetY - y) < 10) {
                targetTank.reduceHealth(50);
                explode();
            }
        } else if (hasImpacted) {
            impactAnimation.update();
        }
    }

    public void explode() {
        if (!hasImpacted) {
            this.active = false;
            this.hasImpacted = true;
            impactAnimation.start();
            Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
            droneSound.stop();
        }
    }

    public void update(List<Rocket> rockets) {
        update();
        rockets.forEach(rocket -> {
            if (rocket.isActive() && getBounds().intersects(rocket.getBounds())) {
                explode();
                rocket.setActive(false);
            }
        });
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, img.getWidth(), img.getHeight());
    }

    public void draw(Graphics2D g2) {
        if (active) {
            AffineTransform translation = AffineTransform.getTranslateInstance(x, y);
            g2.drawImage(this.img, translation, null);
        } else if (hasImpacted) {
            impactAnimation.draw(g2, (int) x, (int) y);
        }
    }
}
