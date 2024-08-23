package TankGame;

import TankGame.game.Audio;
import TankGame.game.GameWorld;
import TankGame.game.StarEffect;
import TankGame.menus.EndGamePanel;
import TankGame.menus.StartMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class Launcher {
    private JPanel mainPanel;
    private GameWorld gamePanel;
    private EndGamePanel endPanel;
    private final JFrame jf;
    private CardLayout cl;

    private Audio titleMusic;
    private Audio gameMusic;
    private Audio endMusic;

    public Launcher() {
        this.jf = new JFrame();
        this.jf.setTitle("Tank Wars Game");
        this.jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.titleMusic = new Audio("tankgame-res/title.wav");
        this.gameMusic = new Audio("tankgame-res/game.wav");
        this.endMusic = new Audio("tankgame-res/endscreen.wav");
        this.gameMusic.setVolume(-9.0f);
        this.endMusic.setVolume(-2.0f);
    }

    private void initUIComponents() {
        this.mainPanel = new JPanel();
        JPanel startPanel = new StartMenuPanel(this);
        this.gamePanel = new GameWorld(this, gameMusic);
        this.gamePanel.initializeGame();
        this.endPanel = new EndGamePanel(this);
        cl = new CardLayout();
        this.mainPanel.setLayout(cl);
        this.mainPanel.add(startPanel, "start");
        this.mainPanel.add(gamePanel, "game");
        this.mainPanel.add(endPanel, "end");
        this.jf.add(mainPanel);
        this.jf.setResizable(false);
        this.setFrame("start");
    }

    public void setFrame(String type) {
        this.jf.setVisible(false);

        titleMusic.stop();
        gameMusic.stop();
        endMusic.stop();
        StarEffect.stopStarSound();

        switch (type) {
            case "start" -> {
                this.jf.setSize(GameConstants.START_MENU_SCREEN_WIDTH, GameConstants.START_MENU_SCREEN_HEIGHT);
                titleMusic.play();
            }
            case "game" -> {
                this.jf.setSize(GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);
                gamePanel.resetGame();
                (new Thread(this.gamePanel)).start();
                gameMusic.play();
            }
            case "end" -> {
                this.jf.setSize(GameConstants.END_MENU_SCREEN_WIDTH, GameConstants.END_MENU_SCREEN_HEIGHT);
                endMusic.play();
            }
        }
        this.cl.show(mainPanel, type);
        this.jf.setVisible(true);
    }

    public void setFrame(String type, String winner) {
        if (type.equals("end")) {
            endPanel.setWinnerText(winner);
        }
        setFrame(type);
    }

    public JFrame getJf() {
        return jf;
    }

    public void closeGame() {
        this.jf.dispatchEvent(new WindowEvent(this.jf, WindowEvent.WINDOW_CLOSING));
    }

    public Audio getGameMusic() {
        return gameMusic;
    }

    public static void main(String[] args) {
        (new Launcher()).initUIComponents();
    }
}
