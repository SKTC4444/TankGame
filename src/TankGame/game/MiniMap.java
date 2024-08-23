package TankGame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class MiniMap extends JPanel {
    private final GameWorld gameWorld;
    private final int miniMapWidth;
    private final int miniMapHeight;

    public MiniMap(GameWorld gameWorld, int miniMapWidth, int miniMapHeight) {
        this.gameWorld = gameWorld;
        this.miniMapWidth = miniMapWidth;
        this.miniMapHeight = miniMapHeight;
        this.setPreferredSize(new Dimension(miniMapWidth, miniMapHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage miniMapImage = new BufferedImage(miniMapWidth, miniMapHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = miniMapImage.createGraphics();

        g2.drawImage(gameWorld.getWorld(), 0, 0, miniMapWidth, miniMapHeight, null);

        double xScale = miniMapWidth / (double) gameWorld.getWorld().getWidth();
        double yScale = miniMapHeight / (double) gameWorld.getWorld().getHeight();

        // Create copies of the lists to avoid ConcurrentModificationException
        List<UnbreakableWall> unbreakableWallsCopy = new ArrayList<>(gameWorld.getUnbreakableWalls());
        List<BreakableWall> breakableWallsCopy = new ArrayList<>(gameWorld.getBreakableWalls());

        for (UnbreakableWall wall : unbreakableWallsCopy) {
            int wallX = (int) (wall.getX() * xScale);
            int wallY = (int) (wall.getY() * yScale);
            int wallWidth = (int) (wall.getImage().getWidth() * xScale);
            int wallHeight = (int) (wall.getImage().getHeight() * yScale);
            g2.drawImage(wall.getImage(), wallX, wallY, wallWidth, wallHeight, null);
        }

        for (BreakableWall wall : breakableWallsCopy) {
            if (!wall.isDestroyed()) {
                int wallX = (int) (wall.getX() * xScale);
                int wallY = (int) (wall.getY() * yScale);
                int wallWidth = (int) (wall.getImage().getWidth() * xScale);
                int wallHeight = (int) (wall.getImage().getHeight() * yScale);
                g2.drawImage(wall.getImage(), wallX, wallY, wallWidth, wallHeight, null);
            }
        }

        int t1X = (int) (gameWorld.getT1().getX() * xScale);
        int t1Y = (int) (gameWorld.getT1().getY() * yScale);
        int t2X = (int) (gameWorld.getT2().getX() * xScale);
        int t2Y = (int) (gameWorld.getT2().getY() * yScale);

        g.drawImage(miniMapImage, 0, 0, null);
    }
}
