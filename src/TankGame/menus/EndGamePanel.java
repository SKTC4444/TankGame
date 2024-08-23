package TankGame.menus;

import TankGame.Launcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class EndGamePanel extends JPanel {
    private BufferedImage menuBackground;
    private final Launcher lf;
    private String winnerText;

    public EndGamePanel(Launcher lf) {
        this.lf = lf;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("tankgame-res/gameover.jpg");
            if (is == null) {
                throw new IOException("Resource not found: tankgame-res/gameover.jpg");
            }
            menuBackground = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("Error: Cannot read menu background");
            e.printStackTrace();
            System.exit(-3);
        }
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        JButton restart = new JButton("Restart Game");
        restart.setFont(new Font("Courier New", Font.BOLD, 24));
        restart.setBounds(150, 300, 250, 50);
        restart.addActionListener(actionEvent -> this.lf.setFrame("game"));

        JButton exit = new JButton("Exit");
        exit.setFont(new Font("Courier New", Font.BOLD, 24));
        exit.setBounds(150, 400, 250, 50);
        exit.addActionListener(actionEvent -> this.lf.closeGame());

        this.add(restart);
        this.add(exit);
    }

    public void setWinnerText(String winnerText) {
        this.winnerText = winnerText;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);
        if (winnerText != null) {
            g2.setFont(new Font("Courier New", Font.BOLD, 36));
            g2.setColor(Color.WHITE);
            g2.drawString(winnerText, 150, 200);
        }
    }
}
