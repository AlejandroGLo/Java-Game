package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

class MyFrame extends JFrame {

    private BufferedImage backgroundImage;
    private Font customFont;

    MyFrame() {
        // Load custom font
        loadCustomFont("fonts/Kranky-Regular.ttf", 32f);

        // Load background image
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/ui/img/background.jpeg"));
        } catch (Exception e) {
            System.err.println("Could not load background image.");
            e.printStackTrace();
        }

        // Set up frame
        setContentPane(new BackgroundPanel());
        setLayout(null);

        // Create and add buttons
        JButton playButton = createTextButton("PLAY", 200);
        JButton optionsButton = createTextButton("OPTIONS", 270);
        JButton exitButton = createTextButton("EXIT", 340);

        add(playButton);
        add(optionsButton);
        add(exitButton);
    }

    private void loadCustomFont(String path, float size) {
        try {
            File fontFile = new File(path);
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            System.err.println("Font load failed. Using default.");
            customFont = new Font("Serif", Font.PLAIN, (int) size);
        }
    }

    private JButton createTextButton(String text, int y) {
        JButton btn = new JButton(text);

        // Get screen size to calculate 20% x-position
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) (screenSize.width * 0.2);

        btn.setBounds(x, y, 200, 40);
        btn.setFont(customFont);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.YELLOW);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setForeground(Color.WHITE); // Corrected from always yellow
            }
        });

        return btn;
    }

    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}

public class MainMenu {
    public static void main(String[] args) {
        MyFrame mf = new MyFrame();
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mf.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mf.setVisible(true);
    }
}