package TankGame.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BreakableWall {
    private int x, y;
    private BufferedImage img;
    private boolean destroyed = false;

    public BreakableWall(int x, int y, BufferedImage img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public BufferedImage getImage() {
        return img;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, img.getWidth(), img.getHeight());
    }

    public void destroy() {
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void draw(Graphics2D g2) {
        if (!destroyed) {
            g2.drawImage(img, x, y, null);
        }
    }
}
