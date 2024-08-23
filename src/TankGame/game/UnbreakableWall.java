package TankGame.game;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UnbreakableWall {
    private int x, y;
    private BufferedImage img;

    public UnbreakableWall(int x, int y, BufferedImage img) {
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

    public void draw(Graphics2D g2) {
        g2.drawImage(img, x, y, null);
    }
}
