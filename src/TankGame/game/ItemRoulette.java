package TankGame.game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class ItemRoulette {
    private List<BufferedImage> itemImages;
    private BufferedImage currentItemImage;
    private boolean active;
    private int currentIndex;
    private long lastUpdateTime;
    private long updateInterval = 100;
    private Random random;
    private long activationStartTime;
    private static final long SHUFFLE_DURATION = 4005;
    private boolean hasItem;
    private Audio shuffleAudio;
    private boolean shuffling;

    public ItemRoulette(List<BufferedImage> itemImages) {
        this.itemImages = itemImages;
        this.currentItemImage = null;
        this.active = false;
        this.currentIndex = 0;
        this.lastUpdateTime = 0;
        this.random = new Random();
        this.hasItem = false;
        this.shuffleAudio = new Audio("tankgame-res/shuffle.wav");
    }

    public void activate() {
        if (!hasItem && !active) {
            this.active = true;
            this.currentItemImage = null;
            this.currentIndex = 0;
            this.lastUpdateTime = System.currentTimeMillis();
            this.activationStartTime = System.currentTimeMillis();
            this.shuffling = true;
            playShuffleSound();
        }
    }

    public void update() {
        if (active) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - activationStartTime >= SHUFFLE_DURATION) {
                currentIndex = random.nextInt(itemImages.size());
                currentItemImage = itemImages.get(currentIndex);
                active = false;
                hasItem = true;
                this.shuffling = false;
                stopShuffleSound();
            } else if (currentTime - lastUpdateTime >= updateInterval) {
                currentIndex = random.nextInt(itemImages.size());
                currentItemImage = itemImages.get(currentIndex);
                lastUpdateTime = currentTime;
            }
        }
    }

    public List<BufferedImage> getItemImages() {
        return itemImages;
    }

    public boolean isShuffling() {
        return shuffling;
    }

    public void draw(Graphics2D g2, int x, int y) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, 70, 70);
        if (currentItemImage != null) {
            g2.drawImage(currentItemImage, x + 5, y + 5, 60, 60, null);
        }
    }

    public BufferedImage getCurrentItemImage() {
        return currentItemImage;
    }

    public boolean hasItem() {
        return hasItem;
    }

    public void useItem() {
        if (hasItem) {
            hasItem = false;
            currentItemImage = null;
        }
    }

    public void clearItem() {
        hasItem = false;
        currentItemImage = null;
        stopShuffling();
    }

    private void stopShuffling() {
        active = false;
        shuffling = false;
        stopShuffleSound();
    }

    private void playShuffleSound() {
        shuffleAudio.play();
    }

    public void stopShuffleSound() {
        shuffleAudio.stop();
    }
}