package TankGame.game;

import TankGame.GameConstants;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Tank {
    private float x, y;
    private float vx, vy;
    private float angle;
    private final float R = 1f;
    private final float ROTATIONSPEED = 1f;
    private final float BOOSTED_R = 1.4f;
    private final float BOOSTED_ROTATIONSPEED = 1.4f;
    private BufferedImage img, shellImg;
    private boolean UpPressed, DownPressed, RightPressed, LeftPressed;
    private long lastShotTime = 0;
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private List<Rocket> rockets = new CopyOnWriteArrayList<>();
    private Animation muzzleFlash;
    private List<Drone> drones = new CopyOnWriteArrayList<>();
    private List<Fireball> fireballs = new CopyOnWriteArrayList<>();
    private MiniGun miniGun;

    private int health = 150;
    private int armor = 0;
    private int lives = 5;
    private final float startX, startY;

    private long respawnTime;
    private boolean invincible;

    private static final int INVINCIBILITY_DURATION = 4000;
    private static final int FLASH_INTERVAL = 250;
    private static final int DEATH_ANIMATION_DELAY = 100;
    private static final int MAX_LIVES = 5;

    private List<BufferedImage> deathAnimationFrames;
    private int deathAnimationIndex;
    private boolean playingDeathAnimation;
    private long deathAnimationStartTime;
    private float deathX, deathY;
    private long lastAnimationFrameTime;

    private ItemRoulette itemRoulette;

    private String message = "";
    private long messageDisplayTime = 0;
    private static final int MESSAGE_DURATION = 2000;

    private static final float DASH_DISTANCE = 4 * 50;
    private static final float DASH_SPEED = 10f;
    private static final int DASH_DURATION = 2000;
    private static final int MINE_LIMIT = 15;
    private boolean dashing = false;
    private long dashStartTime;
    private float dashStartX, dashStartY;

    private List<Mine> mines = new CopyOnWriteArrayList<>();
    private Color ownerColor;

    private List<BufferedImage> lightningFrames;
    private int lightningFrameIndex;
    private boolean lightningActive;
    private long lastLightningFrameTime;
    private static final int LIGHTNING_FRAME_DURATION = 75;
    private float lightningX, lightningY;
    private Tank otherTank;
    private boolean stunned;
    private long stunStartTime;
    private static final int STUN_DURATION = 3500;
    private StarEffect starEffect;
    private BufferedImage fireballImage;
    private List<BufferedImage> fireballExplosionFrames;
    private Airstrike airstrike;

    public Tank(float x, float y, float vx, float vy, float angle, BufferedImage img, List<BufferedImage> deathAnimationFrames, ItemRoulette itemRoulette, Color ownerColor, Audio gameMusic) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.angle = angle;
        this.img = img;
        this.deathAnimationFrames = resizeFrames(deathAnimationFrames, img.getWidth() * 2, img.getHeight() * 2);
        this.startX = x;
        this.startY = y;
        this.muzzleFlash = new Animation("tankgame-res/explosion_sm", "explosion_sm_", 6, 50);
        this.itemRoulette = itemRoulette;
        this.ownerColor = ownerColor;
        this.starEffect = new StarEffect(gameMusic);
        this.miniGun = new MiniGun(this);
        this.airstrike = new Airstrike("tankgame-res/strafeexplosion.gif", 100);
        this.fireballs = new CopyOnWriteArrayList<>();

        try {
            this.shellImg = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/Shell2.gif"), "Shell image not found"));
        } catch (IOException e) {
            System.out.println("Error loading shell image: " + e);
        }

        this.lightningFrames = loadGifFrames("tankgame-res/lightning.gif", img.getWidth() * 10, img.getHeight() * 10);
        this.lightningFrameIndex = 0;
        this.lightningActive = false;
        this.lastLightningFrameTime = 0;
        this.stunned = false;

        try {
            fireballImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/fireballs.png")));
            fireballExplosionFrames = loadGifFrames("tankgame-res/fireexplosion.gif", 180, 180);
        } catch (IOException e) {
            System.out.println("Error loading fireball images: " + e);
        }
    }

    private List<BufferedImage> resizeFrames(List<BufferedImage> frames, int targetWidth, int targetHeight) {
        List<BufferedImage> resizedFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            Image resultingImage = frame.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(resultingImage, 0, 0, null);
            g2d.dispose();
            resizedFrames.add(outputImage);
        }
        return resizedFrames;
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

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getHealth() {
        return health;
    }

    public int getArmor() {
        return armor;
    }

    public List<Bullet> getBullets() {
        return this.bullets;
    }

    public List<Mine> getMines() {
        return mines;
    }

    public List<Rocket> getRockets() {
        return this.rockets;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, img.getWidth(), img.getHeight());
    }

    public void reduceHealth(int amount) {
        if (!invincible && !starEffect.isActive()) {
            if (armor > 0) {
                int remainingDamage = amount - armor;
                armor = Math.max(armor - amount, 0);
                if (remainingDamage > 0) {
                    health -= remainingDamage;
                }
                if (armor == 0) {
                    Audio.playSound("tankgame-res/armorbreak.wav", +5.5f);
                }
            } else {
                health -= amount;
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (playingDeathAnimation) {
            if (System.currentTimeMillis() - deathAnimationStartTime < 1000) {
                if (System.currentTimeMillis() - lastAnimationFrameTime >= DEATH_ANIMATION_DELAY) {
                    lastAnimationFrameTime = System.currentTimeMillis();
                    if (deathAnimationIndex < deathAnimationFrames.size()) {
                        g2.drawImage(deathAnimationFrames.get(deathAnimationIndex), (int) deathX, (int) deathY, null);
                        deathAnimationIndex++;
                    } else {
                        deathAnimationIndex = 0;
                    }
                } else {
                    if (deathAnimationIndex < deathAnimationFrames.size()) {
                        g2.drawImage(deathAnimationFrames.get(deathAnimationIndex), (int) deathX, (int) deathY, null);
                    }
                }
            } else {
                playingDeathAnimation = false;
            }
        }

        for (Mine mine : mines) {
            mine.draw(g2);
        }

        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);

        long currentTime = System.currentTimeMillis();
        if (!invincible || ((currentTime - respawnTime) / FLASH_INTERVAL) % 2 == 0) {
            if (stunned) {
                g2.drawImage(createBlackImage(), rotation, null);
            } else if (starEffect.isActive()) {
                g2.drawImage(starEffect.applyEffect(this.img), rotation, null);
                starEffect.drawSparkles(g2, (int) x, (int) y, img.getWidth(), img.getHeight());
            } else {
                g2.drawImage(this.img, rotation, null);
            }
        }

        if (!muzzleFlash.isFinished()) {
            float tankCenterX = x + img.getWidth() / 2.0f;
            float tankCenterY = y + img.getHeight() / 2.0f;
            float barrelEndX = tankCenterX + (float) Math.cos(Math.toRadians(angle)) * img.getWidth() / 2.0f;
            float barrelEndY = tankCenterY + (float) Math.sin(Math.toRadians(angle)) * img.getHeight() / 2.0f;
            muzzleFlash.draw(g2, (int) (barrelEndX - muzzleFlash.getFrameWidth() / 2.0f), (int) (barrelEndY - muzzleFlash.getFrameHeight() / 2.0f));
        }
        if (!message.isEmpty() && currentTime - messageDisplayTime < MESSAGE_DURATION) {
            g2.setColor(message.equals("Armor Up") ? Color.BLUE : (message.equals("1 Up") ? Color.GREEN : Color.RED));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            g2.drawString(message, (int) (x + img.getWidth() / 2 - textWidth / 2), (int) y - 10);
        }

        if (lightningActive && !lightningFrames.isEmpty()) {
            if (currentTime - lastLightningFrameTime >= LIGHTNING_FRAME_DURATION) {
                lightningFrameIndex++;
                if (lightningFrameIndex >= lightningFrames.size()) {
                    lightningActive = false;
                }
                lastLightningFrameTime = currentTime;
            }
            if (lightningFrameIndex < lightningFrames.size()) {
                g2.drawImage(lightningFrames.get(lightningFrameIndex), (int) lightningX, (int) lightningY, null);
            }
        }

        for (Drone drone : drones) {
            drone.draw(g2);
        }

        Iterator<Rocket> rocketIterator = rockets.iterator();
        while (rocketIterator.hasNext()) {
            Rocket rocket = rocketIterator.next();
            rocket.draw(g2);
            if (!rocket.isActive()) {
                rocketIterator.remove();
            }
        }

        for (Fireball fireball : fireballs) {
            fireball.draw(g2);
        }

        if (miniGun != null) {
            miniGun.draw(g2);
        }
        airstrike.draw(g2);
    }

    private BufferedImage createBlackImage() {
        BufferedImage blackImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = blackImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, blackImage.getWidth(), blackImage.getHeight());
        g2d.dispose();
        return blackImage;
    }

    public void useItem() {
        if (stunned) {
            System.out.println("Cannot use item while stunned.");
            return;
        }
        if (!itemRoulette.isShuffling()) {
            BufferedImage item = itemRoulette.getCurrentItemImage();
            if (item != null) {
                if (item.equals(itemRoulette.getItemImages().get(0))) {
                    this.health = Math.min(this.health + 150, 150);
                    Audio.playSound("tankgame-res/heart.wav");
                    message = "Health Up";
                    messageDisplayTime = System.currentTimeMillis();
                } else if (item.equals(itemRoulette.getItemImages().get(1))) {
                    if (this.lives < MAX_LIVES) {
                        this.lives = Math.min(this.lives + 1, MAX_LIVES);
                        Audio.playSound("tankgame-res/1up.wav");
                        message = "1 Up";
                        messageDisplayTime = System.currentTimeMillis();
                    } else {
                        Audio.playSound("tankgame-res/1up.wav");
                        message = "1 Up";
                        messageDisplayTime = System.currentTimeMillis();
                    }
                } else if (item.equals(itemRoulette.getItemImages().get(2))) {
                    activateDash();
                } else if (item.equals(itemRoulette.getItemImages().get(6))) {
                    this.armor = Math.min(this.armor + 100, 100);
                    Audio.playSound("tankgame-res/armor.wav");
                    message = "Armor Up";
                    messageDisplayTime = System.currentTimeMillis();
                } else if (item.equals(itemRoulette.getItemImages().get(7))) {
                    placeMine();
                } else if (item.equals(itemRoulette.getItemImages().get(5))) {
                    playLightningEffect();
                } else if (item.equals(itemRoulette.getItemImages().get(4))) {
                    starEffect.activate();
                } else if (item.equals(itemRoulette.getItemImages().get(9))) {
                    drones.add(new Drone(x, y, angle, otherTank));
                } else if (item.equals(itemRoulette.getItemImages().get(10))) {
                    launchRocket();
                } else if (item.equals(itemRoulette.getItemImages().get(8))) {
                    if (this.miniGun == null) {
                        this.miniGun = new MiniGun(this);
                    }
                    this.miniGun.start();
                } else if (item.equals(itemRoulette.getItemImages().get(3))) {
                    Audio.playSound("tankgame-res/fireball.wav");
                    launchFireball();
                } else if (item.equals(itemRoulette.getItemImages().get(11))) {
                    Audio.playSound("tankgame-res/strafe.wav", + 6.0f);
                    triggerAirstrike();
                }
                itemRoulette.useItem();
            }
        } else {
            System.out.println("Item cannot be used while shuffling.");
        }
    }

    private void triggerAirstrike() {
        if (otherTank != null) {
            airstrike.start(otherTank.getX(), otherTank.getY(), 150, 50, 1000);
        }
    }

    public void launchRocket() {
        float tankCenterX = x + img.getWidth() / 2.0f;
        float tankCenterY = y + img.getHeight() / 2.0f;
        float barrelEndX = tankCenterX + (float) Math.cos(Math.toRadians(angle)) * img.getWidth() / 2.0f;
        float barrelEndY = tankCenterY + (float) Math.sin(Math.toRadians(angle)) * img.getHeight() / 2.0f;
        BufferedImage rocketImg = itemRoulette.getItemImages().get(10);
        Rocket rocket = new Rocket(barrelEndX - rocketImg.getWidth() / 2.0f, barrelEndY - rocketImg.getHeight() / 2.0f, angle, rocketImg);
        rockets.add(rocket);
    }

    public void update(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank, List<Drone> drones) {
        this.otherTank = otherTank;
        float prevX = x;
        float prevY = y;

        if (stunned) {
            if (System.currentTimeMillis() - stunStartTime >= STUN_DURATION) {
                stunned = false;
            }
        } else {
            if (this.UpPressed) {
                this.moveForwards();
            }
            if (this.DownPressed) {
                this.moveBackwards();
            }
            if (this.LeftPressed) {
                this.rotateLeft();
            }
            if (this.RightPressed) {
                this.rotateRight();
            }
        }

        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update(unbreakableWalls, breakableWalls, otherTank, mines, drones);
            if (!bullet.isActive()) {
                bulletsToRemove.add(bullet);
            }
        }
        bullets.removeAll(bulletsToRemove);

        List<Rocket> rocketsToRemove = new ArrayList<>();
        for (Rocket rocket : rockets) {
            List<Mine> allMines = new ArrayList<>(this.mines);
            allMines.addAll(otherTank.getMines());
            rocket.update(unbreakableWalls, breakableWalls, otherTank, drones, allMines);
            if (!rocket.isActive()) {
                rocketsToRemove.add(rocket);
            }
        }
        rockets.removeAll(rocketsToRemove);

        List<Fireball> fireballsToRemove = new ArrayList<>();
        for (Fireball fireball : fireballs) {
            List<Mine> allMines = new ArrayList<>(this.mines);
            allMines.addAll(otherTank.getMines());
            fireball.update(unbreakableWalls, breakableWalls, otherTank, drones, mines);
            if (!fireball.isActive()) {
                fireballsToRemove.add(fireball);
            }
        }
        fireballs.removeAll(fireballsToRemove);

        List<Mine> minesToRemove = new ArrayList<>();
        for (Mine mine : mines) {
            mine.update(unbreakableWalls, breakableWalls, otherTank, bullets);
            if (!mine.isActive()) {
                minesToRemove.add(mine);
            }
        }
        mines.removeAll(minesToRemove);

        List<Round> rounds = miniGun != null ? miniGun.getRounds() : new ArrayList<>();
        List<Round> roundsToRemove = new ArrayList<>();
        for (Round round : rounds) {
            List<Mine> allMines = new ArrayList<>(this.mines);
            allMines.addAll(otherTank.getMines());
            round.update(unbreakableWalls, breakableWalls, otherTank, allMines, drones);
            if (!round.isActive()) {
                roundsToRemove.add(round);
            }
        }
        rounds.removeAll(roundsToRemove);

        List<Mine> allMines = new ArrayList<>(this.mines);
        allMines.addAll(otherTank.getMines());
        airstrike.update(breakableWalls, otherTank, drones, allMines);
        mines.removeIf(mine -> !mine.isActive());

        if (checkCollision(unbreakableWalls, breakableWalls, otherTank)) {
            x = prevX;
            y = prevY;
        }

        if (invincible && checkOverlap(otherTank)) {
            moveApart(otherTank);
        }

        checkInvincibility();
        muzzleFlash.update();
        updateDash(unbreakableWalls, breakableWalls, otherTank);
        starEffect.update();

        if (lightningActive && !lightningFrames.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLightningFrameTime >= LIGHTNING_FRAME_DURATION) {
                lightningFrameIndex++;
                if (lightningFrameIndex >= lightningFrames.size()) {
                    lightningActive = false;
                }
                lastLightningFrameTime = currentTime;
            }
        }

        if (miniGun != null) {
            miniGun.update(unbreakableWalls, breakableWalls, otherTank, mines, drones);
        }
        updateDrones();
    }

    private boolean checkCollision(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank) {
        Rectangle futureBounds = getBounds();

        for (UnbreakableWall wall : unbreakableWalls) {
            if (futureBounds.intersects(wall.getBounds())) {
                return true;
            }
        }

        for (BreakableWall wall : breakableWalls) {
            if (futureBounds.intersects(wall.getBounds())) {
                return true;
            }
        }

        if (futureBounds.intersects(otherTank.getBounds())) {
            return true;
        }

        return false;
    }

    private boolean checkOverlap(Tank otherTank) {
        Rectangle thisBounds = getBounds();
        Rectangle otherBounds = otherTank.getBounds();
        return thisBounds.intersects(otherBounds);
    }

    private void moveApart(Tank otherTank) {
        float distance = 5f;
        while (checkOverlap(otherTank)) {
            this.x += distance;
            otherTank.x -= distance;
        }
    }

    public void setUpPressed() {
        this.UpPressed = true;
    }

    public void setDownPressed() {
        this.DownPressed = true;
    }

    public void setLeftPressed() {
        this.LeftPressed = true;
    }

    public void setRightPressed() {
        this.RightPressed = true;
    }

    public void setUpReleased() {
        this.UpPressed = false;
    }

    public void setDownReleased() {
        this.DownPressed = false;
    }

    public void setLeftReleased() {
        this.LeftPressed = false;
    }

    public void setRightReleased() {
        this.RightPressed = false;
    }

    public void shoot() {
        if (stunned) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= 1000) {
            float tankCenterX = x + img.getWidth() / 2.0f;
            float tankCenterY = y + img.getHeight() / 2.0f;
            float barrelEndX = tankCenterX + (float) Math.cos(Math.toRadians(angle)) * img.getWidth() / 2.0f;
            float barrelEndY = tankCenterY + (float) Math.sin(Math.toRadians(angle)) * img.getHeight() / 2.0f;
            Bullet bullet = new Bullet(barrelEndX - shellImg.getWidth() / 2.0f, barrelEndY - shellImg.getHeight() / 2.0f, angle, shellImg);
            bullets.add(bullet);
            lastShotTime = currentTime;
            if (!stunned) {
                Audio.playSound("tankgame-res/Explosion_small.wav", -14.0f);
            }
            muzzleFlash.start();
        }
    }

    private void moveForwards() {
        float speed = starEffect.isActive() ? BOOSTED_R : R;
        vx = (float) Math.cos(Math.toRadians(angle)) * speed;
        vy = (float) Math.sin(Math.toRadians(angle)) * speed;
        x += vx;
        y += vy;
    }

    private void moveBackwards() {
        float speed = starEffect.isActive() ? BOOSTED_R : R;
        vx = (float) Math.cos(Math.toRadians(angle)) * speed;
        vy = (float) Math.sin(Math.toRadians(angle)) * speed;
        x -= vx;
        y -= vy;
    }

    private void rotateLeft() {
        float rotationSpeed = starEffect.isActive() ? BOOSTED_ROTATIONSPEED : ROTATIONSPEED;
        angle -= rotationSpeed;
    }

    private void rotateRight() {
        float rotationSpeed = starEffect.isActive() ? BOOSTED_ROTATIONSPEED : ROTATIONSPEED;
        angle += rotationSpeed;
    }

    public void respawn() {
        this.x = startX;
        this.y = startY;
        this.health = 150;
        this.armor = 0;
        this.respawnTime = System.currentTimeMillis();
        this.invincible = true;
        this.stunned = false;
        this.dashing = false;
        if (this.miniGun != null) {
            this.miniGun.stop();
            this.miniGun = null;
        }
    }

    public void playDeathAnimation() {
        deathX = x;
        deathY = y;
        playingDeathAnimation = true;
        deathAnimationStartTime = System.currentTimeMillis();
        deathAnimationIndex = 0;
        Audio.playSound("tankgame-res/death.wav", - 2.5f);
        itemRoulette.clearItem();
        if (this.miniGun != null) {
            this.miniGun.stop();
            this.miniGun = null;
        }
    }

    private void checkInvincibility() {
        if (invincible && System.currentTimeMillis() - respawnTime > INVINCIBILITY_DURATION) {
            invincible = false;
        }
    }

    private void activateDash() {
        if (!dashing) {
            dashing = true;
            dashStartTime = System.currentTimeMillis();
            dashStartX = x;
            dashStartY = y;
            Audio.playSound("tankgame-res/dash.wav", 2.0f);
        }
    }

    private void updateDash(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank) {
        if (dashing) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - dashStartTime < DASH_DURATION && calculateDistance(dashStartX, dashStartY, x, y) < DASH_DISTANCE) {
                float tempX = x + (float) Math.cos(Math.toRadians(angle)) * DASH_SPEED;
                float tempY = y + (float) Math.sin(Math.toRadians(angle)) * DASH_SPEED;
                Rectangle futureBounds = new Rectangle((int) tempX, (int) tempY, img.getWidth(), img.getHeight());

                boolean collision = false;

                for (UnbreakableWall wall : unbreakableWalls) {
                    if (futureBounds.intersects(wall.getBounds())) {
                        collision = true;
                        break;
                    }
                }

                for (BreakableWall wall : breakableWalls) {
                    if (futureBounds.intersects(wall.getBounds())) {
                        collision = true;
                        break;
                    }
                }

                if (futureBounds.intersects(otherTank.getBounds())) {
                    collision = true;
                }

                if (!collision) {
                    x = tempX;
                    y = tempY;
                } else {
                    dashing = false;
                }

                x = Math.min(Math.max(x, 0), GameConstants.GAME_WORLD_WIDTH - img.getWidth());
                y = Math.min(Math.max(y, 0), GameConstants.GAME_WORLD_HEIGHT - img.getHeight());
            } else {
                dashing = false;
            }
        }
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public void placeMine() {
        if (mines.size() >= MINE_LIMIT) {
            mines.remove(0);
        }
        Mine mine = new Mine(x, y, itemRoulette.getItemImages().get(7), ownerColor);
        mines.add(mine);
        Audio.playSound("tankgame-res/minedeployed.wav");
    }

    public void playLightningEffect() {
        if (otherTank != null) {
            otherTank.reduceHealth(100);
            otherTank.stunTank();
            otherTank.loseMiniGun();
            lightningX = (float) (otherTank.getX() - (lightningFrames.get(0).getWidth() / 2.198));
            lightningY = (float) (otherTank.getY() - (lightningFrames.get(0).getHeight() / 1.17));
        }
        Audio.playSound("tankgame-res/lightning.wav", - 3.5f);
        lightningFrameIndex = 0;
        lightningActive = true;
        lastLightningFrameTime = System.currentTimeMillis();
    }

    public void updateDrones() {
        for (Drone drone : drones) {
            drone.update(rockets);
        }
        drones.removeIf(drone -> !drone.isActive());
    }

    public void stunTank() {
        if (!invincible && !starEffect.isActive()) {
            this.stunned = true;
            this.stunStartTime = System.currentTimeMillis();
        }
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public float getAngle() {
        return this.angle;
    }

    public int getWidth() {
        return this.img.getWidth();
    }

    public int getHeight() {
        return this.img.getHeight();
    }

    public MiniGun getMiniGun() {
        return this.miniGun;
    }

    public void stopMiniGun() {
        if (this.miniGun != null) {
            this.miniGun.stop();
        }
    }

    public void loseMiniGun() {
        if (this.miniGun != null) {
            this.miniGun.stop();
            this.miniGun = null;
        }
    }

    public void launchFireball() {
        float tankCenterX = x + img.getWidth() / 2.0f;
        float tankCenterY = y + img.getHeight() / 2.0f;
        float offset = 20.0f;
        float barrelEndX = tankCenterX + (float) Math.cos(Math.toRadians(angle)) * (img.getWidth() / 2.0f + offset);
        float barrelEndY = tankCenterY + (float) Math.sin(Math.toRadians(angle)) * (img.getHeight() / 2.0f + offset);

        Fireball fireballCenter = new Fireball(
                barrelEndX,
                barrelEndY,
                angle,
                fireballImage,
                fireballExplosionFrames
        );
        fireballCenter.setImageScale(0.1f, 0.1f);
        fireballs.add(fireballCenter);


        Fireball fireballLeft = new Fireball(
                barrelEndX,
                barrelEndY,
                angle - 10,
                fireballImage,
                fireballExplosionFrames
        );
        fireballLeft.setImageScale(0.1f, 0.1f);
        fireballs.add(fireballLeft);

        Fireball fireballRight = new Fireball(
                barrelEndX,
                barrelEndY,
                angle + 10,
                fireballImage,
                fireballExplosionFrames
        );
        fireballRight.setImageScale(0.1f, 0.1f);
        fireballs.add(fireballRight);
    }

    public List<Fireball> getFireballs() {
        return fireballs;
    }

}
