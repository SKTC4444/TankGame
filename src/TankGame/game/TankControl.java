package TankGame.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TankControl implements KeyListener {
    private Tank tank;
    private final int up;
    private final int down;
    private final int left;
    private final int right;
    private final int shoot;
    private final int useItem;

    public TankControl(Tank tank, int up, int down, int left, int right, int shoot, int useItem) {
        this.tank = tank;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.shoot = shoot;
        this.useItem = useItem;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == up) {
            this.tank.setUpPressed();
        }
        if (key == down) {
            this.tank.setDownPressed();
        }
        if (key == left) {
            this.tank.setLeftPressed();
        }
        if (key == right) {
            this.tank.setRightPressed();
        }
        if (key == shoot) {
            this.tank.shoot();
        }
        if (key == useItem) {
            this.tank.useItem();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == up) {
            this.tank.setUpReleased();
        }
        if (key == down) {
            this.tank.setDownReleased();
        }
        if (key == left) {
            this.tank.setLeftReleased();
        }
        if (key == right) {
            this.tank.setRightReleased();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
