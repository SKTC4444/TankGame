package TankGame.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class Mine {
    private float x, y;
    private BufferedImage img;
    private boolean active;
    private Color ownerColor;
    private Animation impactAnimation;
    private boolean hasImpacted = false;
    private static final int MINE_INDICATOR_OFFSET = 20;

    public Mine(float x, float y, BufferedImage img, Color ownerColor) {
        this.x = x;
        this.y = y;
        this.img = resizeImage(img, (int) (img.getWidth() / 1.5), (int) (img.getHeight() / 1.5));
        this.ownerColor = ownerColor;
        this.active = true;
        this.impactAnimation = new Animation("tankgame-res/explosion_lg", "explosion_lg_", 6, 50);
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

    public void update(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank tank, List<Bullet> bullets) {
        if (hasImpacted) {
            impactAnimation.update();
        } else {
            checkProximity(tank);
            bullets.forEach(bullet -> {
                if (bullet.isActive() && getBounds().intersects(bullet.getBounds())) {
                    explode();
                    bullet.setActive(false);
                }
            });
        }
    }

    public void explode() {
        if (!hasImpacted) {
            this.active = false;
            this.hasImpacted = true;
            impactAnimation.start();
            Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
        }
    }

    public Rectangle getBounds() {
        Rectangle bounds = new Rectangle((int) x, (int) y, img.getWidth(), img.getHeight());
        return bounds;
    }

    public void draw(Graphics2D g2) {
        if (active) {
            g2.drawImage(img, (int) x, (int) y, null);
            g2.setColor(ownerColor);
            g2.fillOval((int) x + img.getWidth() / 2 - 5, (int) y - MINE_INDICATOR_OFFSET, 10, 10);
        } else if (hasImpacted) {
            impactAnimation.draw(g2, (int) x, (int) y);
        }
    }

    public void checkProximity(Tank tank) {
        double detectionRadius = Math.max(impactAnimation.getFrameWidth(), impactAnimation.getFrameHeight()) * 1.25;
        double distance = Math.sqrt(Math.pow(x - tank.getX(), 2) + Math.pow(y - tank.getY(), 2));

        if (distance < detectionRadius) {
            explode();
            tank.reduceHealth(50);
        }
    }
}
