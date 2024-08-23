package TankGame.menus;

import TankGame.Launcher;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class StartMenuPanel extends JPanel {
    private BufferedImage menuBackground;
    private final Launcher lf;

    public StartMenuPanel(Launcher lf) {
        this.lf = lf;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("tankgame-res/Title.bmp");
            if (is == null) {
                throw new IOException("Resource not found: tankgame-res/Title.bmp");
            }
            menuBackground = ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("Error: Cannot read menu background");
            e.printStackTrace();
            System.exit(-3);
        }
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        JButton start = new JButton("Start");
        start.setFont(new Font("Courier New", Font.BOLD, 24));
        start.setBounds(150, 300, 150, 50);
        start.addActionListener(actionEvent -> this.lf.setFrame("game"));

        JButton exit = new JButton("Exit");
        exit.setSize(new Dimension(200, 100));
        exit.setFont(new Font("Courier New", Font.BOLD, 24));
        exit.setBounds(150, 400, 150, 50);
        exit.addActionListener(actionEvent -> this.lf.closeGame());

        this.add(start);
        this.add(exit);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.menuBackground, 0, 0, null);
    }
}