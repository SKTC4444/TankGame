package TankGame.game;

import TankGame.GameConstants;
import TankGame.Launcher;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameWorld extends JPanel implements Runnable {
    private static List<UnbreakableWall> unbreakableWalls = new ArrayList<>();
    private static List<BreakableWall> breakableWalls = new ArrayList<>();
    private final Audio gameMusic;
    private List<ItemBox> itemBoxes = new ArrayList<>();
    private BufferedImage world;
    private BufferedImage p1Camera;
    private BufferedImage p2Camera;
    private BufferedImage backgroundImage;
    private List<BufferedImage> deathAnimationFrames;
    private Tank t1, t2;
    private final Launcher lf;
    private MiniMap miniMap;
    private boolean gameOver;
    private String winner;
    private Graphics2D buffer;
    private static boolean isMusicPlaying = false;
    private ItemRoulette itemRoulette1;
    private ItemRoulette itemRoulette2;

    public GameWorld(Launcher lf, Audio gameMusic) {
        this.lf = lf;
        this.gameMusic = gameMusic;
        initializeGame();
    }

    public BufferedImage getWorld() {
        return world;
    }

    public Tank getT1() {
        return t1;
    }

    public Tank getT2() {
        return t2;
    }

    public static List<UnbreakableWall> getUnbreakableWalls() {
        return unbreakableWalls;
    }

    public static List<BreakableWall> getBreakableWalls() {
        return breakableWalls;
    }

    public void initializeGame() {
        try {
            world = new BufferedImage(GameConstants.GAME_WORLD_WIDTH, GameConstants.GAME_WORLD_HEIGHT, BufferedImage.TYPE_INT_RGB);
            backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/Background.png"), "Background image file not found"));
            BufferedImage t1img = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/tank1.png"), "Tank1 image file not found"));
            BufferedImage t2img = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/tank2.png"), "Tank2 image file not found"));
            deathAnimationFrames = loadGifFrames("tankgame-res/death.gif", (int) (t1img.getWidth() * 1.5), (int) (t1img.getHeight() * 1.5));

            List<BufferedImage> itemImages = new ArrayList<>();

            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/heart.gif"), "Heart image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/1up.png"), "1 Up image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/dash.png"), "Dash image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/fireflower.png"), "Fire flower image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/star.png"), "Star image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/lightningbolt.png"), "Lightning bolt image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/armor.png"), "Armor image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/mine.png"), "Mine image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/minigun.png"), "Minigun image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/drone.png"), "Drone image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/rocket.png"), "Rocket image not found")), 60, 60));
            itemImages.add(resizeImage(ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/jet.png"), "Jet image not found")), 60, 60));

            itemRoulette1 = new ItemRoulette(itemImages);
            itemRoulette2 = new ItemRoulette(itemImages);

            t1 = new Tank(300, 300, 0, 0, 0, t1img, deathAnimationFrames, itemRoulette1, Color.RED, gameMusic);
            t2 = new Tank(900, 900, 0, 0, 0, t2img, deathAnimationFrames, itemRoulette2, Color.BLUE, gameMusic);

            TankControl tc1 = new TankControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_F);
            TankControl tc2 = new TankControl(t2, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, KeyEvent.VK_L);

            this.lf.getJf().addKeyListener(tc1);
            this.lf.getJf().addKeyListener(tc2);

            populateWalls();
            populateItemBoxes();

            int miniMapWidth = 200;
            int miniMapHeight = 150;
            miniMap = new MiniMap(this, miniMapWidth, miniMapHeight);
            this.setLayout(null);
            int miniMapX = (GameConstants.GAME_SCREEN_WIDTH - miniMapWidth) / 2;
            int miniMapY = GameConstants.GAME_SCREEN_HEIGHT - miniMapHeight - 20;
            miniMap.setBounds(miniMapX, miniMapY, miniMapWidth, miniMapHeight);
            this.add(miniMap);

            gameOver = false;
            winner = null;

        } catch (IOException e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void populateWalls() {
        unbreakableWalls.clear();
        breakableWalls.clear();
        try {
            BufferedImage wallImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/wall1.png"), "Unbreakable wall image not found"));
            BufferedImage breakableWallImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/wall2.png"), "Breakable wall image not found"));

            int wallWidth = wallImage.getWidth();
            int wallHeight = wallImage.getHeight();

            for (int x = 0; x < GameConstants.GAME_WORLD_WIDTH; x += wallWidth) {
                unbreakableWalls.add(new UnbreakableWall(x, 0, wallImage));
                unbreakableWalls.add(new UnbreakableWall(x, GameConstants.GAME_WORLD_HEIGHT - wallHeight, wallImage));
            }

            for (int y = 0; y < GameConstants.GAME_WORLD_HEIGHT; y += wallHeight) {
                unbreakableWalls.add(new UnbreakableWall(0, y, wallImage));
                unbreakableWalls.add(new UnbreakableWall(GameConstants.GAME_WORLD_WIDTH - wallWidth, y, wallImage));
            }

            for (int x = 500; x <= 700; x += wallWidth) {
                unbreakableWalls.add(new UnbreakableWall(x, 600, wallImage));
            }
            for (int y = 500; y <= 700; y += wallHeight) {
                unbreakableWalls.add(new UnbreakableWall(600, y, wallImage));
            }

            breakableWalls.add(new BreakableWall(200, 200, breakableWallImage));
            breakableWalls.add(new BreakableWall(200, 400, breakableWallImage));
            breakableWalls.add(new BreakableWall(200, 600, breakableWallImage));
            breakableWalls.add(new BreakableWall(200, 800, breakableWallImage));
            breakableWalls.add(new BreakableWall(400, 200, breakableWallImage));
            breakableWalls.add(new BreakableWall(400, 400, breakableWallImage));
            breakableWalls.add(new BreakableWall(400, 600, breakableWallImage));
            breakableWalls.add(new BreakableWall(400, 800, breakableWallImage));
            breakableWalls.add(new BreakableWall(600, 200, breakableWallImage));
            breakableWalls.add(new BreakableWall(600, 800, breakableWallImage));
            breakableWalls.add(new BreakableWall(800, 200, breakableWallImage));
            breakableWalls.add(new BreakableWall(800, 400, breakableWallImage));
            breakableWalls.add(new BreakableWall(800, 600, breakableWallImage));
            breakableWalls.add(new BreakableWall(800, 800, breakableWallImage));
            breakableWalls.add(new BreakableWall(1000, 200, breakableWallImage));
            breakableWalls.add(new BreakableWall(1000, 400, breakableWallImage));
            breakableWalls.add(new BreakableWall(1000, 600, breakableWallImage));
            breakableWalls.add(new BreakableWall(1000, 800, breakableWallImage));

        } catch (IOException e) {
            System.err.println("Failed to load wall images: " + e.getMessage());
        }
    }

    private void populateItemBoxes() {
        itemBoxes.clear();
        Random rand = new Random();
        for (int i = 0; i < 2; i++) {
            float x = rand.nextInt(GameConstants.GAME_WORLD_WIDTH / 2);
            float y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
            while (isPositionOccupied(x, y)) {
                x = rand.nextInt(GameConstants.GAME_WORLD_WIDTH / 2);
                y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
            }
            itemBoxes.add(new ItemBox(x, y, "tankgame-res/itembox.gif"));
        }
        for (int i = 0; i < 2; i++) {
            float x = GameConstants.GAME_WORLD_WIDTH / 2 + rand.nextInt(GameConstants.GAME_WORLD_WIDTH / 2);
            float y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
            while (isPositionOccupied(x, y)) {
                x = GameConstants.GAME_WORLD_WIDTH / 2 + rand.nextInt(GameConstants.GAME_WORLD_WIDTH / 2);
                y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
            }
            itemBoxes.add(new ItemBox(x, y, "tankgame-res/itembox.gif"));
        }
    }

    private boolean isPositionOccupied(float x, float y) {
        Rectangle testRect = new Rectangle((int) x, (int) y, 50, 50);
        for (UnbreakableWall wall : unbreakableWalls) {
            if (testRect.intersects(wall.getBounds())) {
                return true;
            }
        }
        for (BreakableWall wall : breakableWalls) {
            if (testRect.intersects(wall.getBounds())) {
                return true;
            }
        }
        if (testRect.intersects(new Rectangle(300, 300, 50, 50)) || testRect.intersects(new Rectangle(900, 900, 50, 50))) {
            return true;
        }
        return false;
    }

    public void resetGame() {
        initializeGame();
        this.lf.getJf().removeKeyListener(this.lf.getJf().getKeyListeners()[0]);
        TankControl tc1 = new TankControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_F);
        TankControl tc2 = new TankControl(t2, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER, KeyEvent.VK_L);
        this.lf.getJf().addKeyListener(tc1);
        this.lf.getJf().addKeyListener(tc2);
    }

    @Override
    public void run() {
        while (true) {
            if (!gameOver) {
                t1.update(unbreakableWalls, breakableWalls, t2, t2.getDrones());
                t2.update(unbreakableWalls, breakableWalls, t1, t1.getDrones());
                itemRoulette1.update();
                itemRoulette2.update();
                checkItemBoxCollisions(t1);
                checkItemBoxCollisions(t2);
                checkMineCollisions(t1, t2);
                checkGameOver();
            }
            repaint();
            breakableWalls.removeIf(BreakableWall::isDestroyed);
            try {
                Thread.sleep(1000 / 144);
            } catch (InterruptedException ignored) {
                System.out.println("Interrupted: " + ignored.getMessage());
            }
        }
    }

    private void checkItemBoxCollisions(Tank tank) {
        for (ItemBox itemBox : itemBoxes) {
            if (itemBox.isActive() && tank.getBounds().intersects(itemBox.getBounds())) {
                itemBox.deactivate();
                respawnItemBox(itemBox);
                if (tank == t1 && !itemRoulette1.hasItem()) {
                    itemRoulette1.activate();
                } else if (tank == t2 && !itemRoulette2.hasItem()) {
                    itemRoulette2.activate();
                }
            }
        }
    }

    private void respawnItemBox(ItemBox itemBox) {
        Random rand = new Random();
        float x = rand.nextInt(GameConstants.GAME_WORLD_WIDTH);
        float y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
        while (isPositionOccupied(x, y)) {
            x = rand.nextInt(GameConstants.GAME_WORLD_WIDTH);
            y = rand.nextInt(GameConstants.GAME_WORLD_HEIGHT);
        }
        itemBox.respawn(x, y);
    }

    private void checkMineCollisions(Tank t1, Tank t2) {
        for (Bullet bullet : t1.getBullets()) {
            checkBulletMineCollision(bullet, t2.getMines());
        }
        for (Bullet bullet : t2.getBullets()) {
            checkBulletMineCollision(bullet, t1.getMines());
        }
    }

    private void checkBulletMineCollision(Bullet bullet, List<Mine> mines) {
        for (Mine mine : mines) {
            if (bullet.getBounds().intersects(mine.getBounds()) && mine.isActive()) {
                bullet.setActive(false);
                mine.explode();
            }
        }
    }

    private void checkGameOver() {
        if (t1.getLives() <= 0) {
            gameOver = true;
            winner = "Tank 2 Wins!";
            t1.stopMiniGun();
            t2.stopMiniGun();
            itemRoulette1.stopShuffleSound();
            itemRoulette2.stopShuffleSound();
            showEndGamePanel();
        } else if (t2.getLives() <= 0) {
            gameOver = true;
            winner = "Tank 1 Wins!";
            t1.stopMiniGun();
            t2.stopMiniGun();
            itemRoulette1.stopShuffleSound();
            itemRoulette2.stopShuffleSound();
            showEndGamePanel();
        } else {
            if (t1.getLives() > 0 && t1.getHealth() <= 0) {
                t1.playDeathAnimation();
                t1.setLives(t1.getLives() - 1);
                t1.respawn();
            }
            if (t2.getLives() > 0 && t2.getHealth() <= 0) {
                t2.playDeathAnimation();
                t2.setLives(t2.getLives() - 1);
                t2.respawn();
            }
        }
    }

    private void showEndGamePanel() {
        lf.setFrame("end", winner);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        buffer = world.createGraphics();
        clearBuffer();
        drawWorld(buffer);
        buffer.dispose();

        Graphics2D g2 = (Graphics2D) g;
        if (!gameOver) {
            p1Camera = world.getSubimage(getCameraX(t1), getCameraY(t1), GameConstants.GAME_SCREEN_WIDTH / 2, GameConstants.GAME_SCREEN_HEIGHT);
            p2Camera = world.getSubimage(getCameraX(t2), getCameraY(t2), GameConstants.GAME_SCREEN_WIDTH / 2, GameConstants.GAME_SCREEN_HEIGHT);

            g2.drawImage(p1Camera, 0, 0, null);
            g2.drawImage(p2Camera, GameConstants.GAME_SCREEN_WIDTH / 2, 0, null);

            drawTankStatus(g2, t1, 10, 10);
            drawTankStatus(g2, t2, GameConstants.GAME_SCREEN_WIDTH / 2 + 10, 10);

            int miniMapX = (GameConstants.GAME_SCREEN_WIDTH - miniMap.getWidth()) / 2;
            int miniMapY = GameConstants.GAME_SCREEN_HEIGHT - miniMap.getHeight() - 20;
            miniMap.setBounds(miniMapX, miniMapY, miniMap.getWidth(), miniMap.getHeight());
            miniMap.repaint();

            itemRoulette1.draw(g2, 10, 70);
            itemRoulette2.draw(g2, GameConstants.GAME_SCREEN_WIDTH / 2 + 10, 70);
        }
    }

    private void clearBuffer() {
        Graphics2D g2 = (Graphics2D) world.getGraphics();
        for (int x = 0; x < GameConstants.GAME_WORLD_WIDTH; x += backgroundImage.getWidth()) {
            for (int y = 0; y < GameConstants.GAME_WORLD_HEIGHT; y += backgroundImage.getHeight()) {
                g2.drawImage(backgroundImage, x, y, null);
            }
        }
        g2.dispose();
    }

    private int getCameraX(Tank tank) {
        return Math.max(0, Math.min((int) tank.getX() - GameConstants.GAME_SCREEN_WIDTH / 4, GameConstants.GAME_WORLD_WIDTH - GameConstants.GAME_SCREEN_WIDTH / 2));
    }

    private int getCameraY(Tank tank) {
        return Math.max(0, Math.min((int) tank.getY() - GameConstants.GAME_SCREEN_HEIGHT / 2, GameConstants.GAME_WORLD_HEIGHT - GameConstants.GAME_SCREEN_HEIGHT));
    }

    private void drawWorld(Graphics2D g2) {
        drawTiles(g2);
        drawMines(g2);
        drawItemBoxes(g2);
        drawProjectiles(g2);
        t1.draw(g2);
        t2.draw(g2);
    }

    private void drawMines(Graphics2D g2) {
        for (Mine mine : t1.getMines()) {
            mine.draw(g2);
        }
        for (Mine mine : t2.getMines()) {
            mine.draw(g2);
        }
    }

    private void drawTiles(Graphics2D g2) {
        for (UnbreakableWall wall : unbreakableWalls) {
            wall.draw(g2);
        }
        for (BreakableWall wall : breakableWalls) {
            wall.draw(g2);
        }
    }

    private void drawProjectiles(Graphics2D g2) {
        List<Bullet> t1BulletsCopy = new ArrayList<>(t1.getBullets());
        List<Bullet> t2BulletsCopy = new ArrayList<>(t2.getBullets());
        List<Rocket> t1RocketsCopy = new ArrayList<>(t1.getRockets());
        List<Rocket> t2RocketsCopy = new ArrayList<>(t2.getRockets());
        List<Round> t1RoundsCopy = t1.getMiniGun() != null ? new ArrayList<>(t1.getMiniGun().getRounds()) : new ArrayList<>();
        List<Round> t2RoundsCopy = t2.getMiniGun() != null ? new ArrayList<>(t2.getMiniGun().getRounds()) : new ArrayList<>();
        List<Fireball> t1FireballsCopy = new ArrayList<>(t1.getFireballs());
        List<Fireball> t2FireballsCopy = new ArrayList<>(t2.getFireballs());

        for (Bullet bullet : t1BulletsCopy) {
            bullet.draw(g2);
        }
        for (Bullet bullet : t2BulletsCopy) {
            bullet.draw(g2);
        }

        for (Rocket rocket : t1RocketsCopy) {
            rocket.draw(g2);
        }
        for (Rocket rocket : t2RocketsCopy) {
            rocket.draw(g2);
        }

        for (Round round : t1RoundsCopy) {
            round.draw(g2);
        }
        for (Round round : t2RoundsCopy) {
            round.draw(g2);
        }

        for (Fireball fireball : t1FireballsCopy) {
            fireball.draw(g2);
        }
        for (Fireball fireball : t2FireballsCopy) {
            fireball.draw(g2);
        }
    }

    private void drawItemBoxes(Graphics2D g2) {
        for (ItemBox itemBox : itemBoxes) {
            itemBox.draw(g2);
        }
    }

    private void drawTankStatus(Graphics2D g2, Tank tank, int x, int y) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, 120, 50);
        g2.setColor(Color.WHITE);
        g2.drawString("Lives: " + tank.getLives(), x + 10, y + 15);

        int healthBarWidth = 100;
        int healthBarHeight = 10;
        int health = tank.getHealth();
        int maxHealth = 150;
        int healthBarCurrentWidth = (int) ((health / (double) maxHealth) * healthBarWidth);

        g2.setColor(Color.RED);
        g2.fillRect(x + 10, y + 20, healthBarWidth, healthBarHeight);
        g2.setColor(Color.GREEN);
        g2.fillRect(x + 10, y + 20, healthBarCurrentWidth, healthBarHeight);
        g2.setColor(Color.BLACK);
        g2.drawRect(x + 10, y + 20, healthBarWidth, healthBarHeight);

        int armor = tank.getArmor();
        if (armor > 0) {
            int armorBarCurrentWidth = (int) ((armor / 100.0) * healthBarWidth);
            g2.setColor(Color.BLUE);
            g2.fillRect(x + 10, y + 32, armorBarCurrentWidth, healthBarHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(x + 10, y + 32, healthBarWidth, healthBarHeight);
        }
    }
}
