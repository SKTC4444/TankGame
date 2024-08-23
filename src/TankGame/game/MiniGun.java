package TankGame.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MiniGun {
    private BufferedImage img;
    private boolean active;
    private long startTime;
    private static final int DURATION = 10000;
    private Tank owner;
    private Audio shootingSound;
    private List<Round> rounds;
    private long lastShotTime;
    private static final int SHOT_INTERVAL = 30;

    public MiniGun(Tank owner) {
        this.owner = owner;
        try {
            BufferedImage originalImg = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("tankgame-res/minigun.png")));
            this.img = resizeImage(originalImg, originalImg.getWidth() / 2, originalImg.getHeight() / 2);
        } catch (IOException e) {
            System.out.println("Error loading minigun image: " + e);
        }
        this.active = false;
        this.shootingSound = new Audio("tankgame-res/minigun.wav");
        this.shootingSound.setVolume(-6.0f);
        this.rounds = new ArrayList<>();
        this.lastShotTime = 0;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();
        return outputImage;
    }

    public void start() {
        this.active = true;
        this.startTime = System.currentTimeMillis();
        this.shootingSound.play();
    }

    public void stop() {
        this.active = false;
        this.shootingSound.stop();
    }

    public boolean isActive() {
        return active;
    }

    public void update(List<UnbreakableWall> unbreakableWalls, List<BreakableWall> breakableWalls, Tank otherTank, List<Mine> mines, List<Drone> drones) {
        if (active && System.currentTimeMillis() - startTime >= DURATION) {
            stop();
        }
        if (active && System.currentTimeMillis() - lastShotTime >= SHOT_INTERVAL) {
            shootRound();
            lastShotTime = System.currentTimeMillis();
        }
        rounds.forEach(round -> round.update(unbreakableWalls, breakableWalls, otherTank, mines, drones));
        rounds.removeIf(round -> !round.isActive());
    }

    private void shootRound() {
        float tankCenterX = owner.getX() + owner.getWidth() / 2.0f;
        float tankCenterY = owner.getY() + owner.getHeight() / 2.0f;
        float barrelEndX = tankCenterX + (float) Math.cos(Math.toRadians(owner.getAngle())) * img.getWidth() / 2.0f;
        float barrelEndY = tankCenterY + (float) Math.sin(Math.toRadians(owner.getAngle())) * img.getHeight() / 2.0f;
        Round round = new Round(barrelEndX - Round.TARGET_WIDTH / 2.0f, barrelEndY - Round.TARGET_HEIGHT / 2.0f, owner.getAngle());
        rounds.add(round);
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void draw(Graphics2D g2) {
        if (active) {
            AffineTransform transform = new AffineTransform();
            float angle = (float) Math.toRadians(owner.getAngle());
            float tankCenterX = owner.getX() + owner.getWidth() / 2.0f;
            float tankCenterY = owner.getY() + owner.getHeight() / 2.0f;
            transform.translate(tankCenterX - img.getWidth() / 2.0f, tankCenterY - img.getHeight() / 2.0f);
            transform.rotate(angle, img.getWidth() / 2.0f, img.getHeight() / 2.0f);
            g2.drawImage(img, transform, null);
        }
    }
}
