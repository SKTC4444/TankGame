package TankGame.game;

import TankGame.GameConstants;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

public class Bullet {
    private float x, y;
    private float angle;
    private BufferedImage img;
    private final float speed = 9.0f;
    private boolean active = true;
    private Animation impactAnimation;
    private boolean hasImpacted = false;

    public Bullet(float x, float y, float angle, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.img = img;
        this.impactAnimation = new Animation("tankgame-res/explosion_lg", "explosion_lg_", 6, 50);
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
                hasImpacted = true;
                otherTank.reduceHealth(50);
                Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
                impactAnimation.start();
                return;
            }

            for (UnbreakableWall wall : unbreakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    active = false;
                    hasImpacted = true;
                    Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
                    impactAnimation.start();
                    return;
                }
            }

            for (BreakableWall wall : breakableWalls) {
                if (getBounds().intersects(wall.getBounds())) {
                    active = false;
                    hasImpacted = true;
                    wall.destroy();
                    Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
                    impactAnimation.start();
                    return;
                }
            }

            for (Mine mine : mines) {
                if (getBounds().intersects(mine.getBounds())) {
                    active = false;
                    hasImpacted = true;
                    mine.explode();
                    Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
                    impactAnimation.start();
                    return;
                }
            }

            for (Drone drone : drones) {
                if (getBounds().intersects(drone.getBounds())) {
                    active = false;
                    hasImpacted = true;
                    drone.explode();
                    Audio.playSound("tankgame-res/Explosion_large.wav", -10.0f);
                    impactAnimation.start();
                    return;
                }
            }
        } else if (hasImpacted) {
            impactAnimation.update();
        }
    }

    public boolean isActive() {
        return active || !impactAnimation.isFinished();
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, img.getWidth(), img.getHeight());
    }

    public void draw(Graphics2D g2) {
        if (active) {
            AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
            rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
            g2.drawImage(this.img, rotation, null);
        } else if (hasImpacted) {
            impactAnimation.draw(g2, (int)x, (int)y);
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
