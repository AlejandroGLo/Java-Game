package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

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

        //Label for Title

        JLabel title = new JLabel("SHOWDOWN");
        title.setFont(customFont.deriveFont(78f));
        title.setForeground(Color.WHITE);
        title.setBounds(150,150,600,70);
        add(title);

        // Create and add buttons
        JButton playButton = createTextButton("PLAY", 340);
        JButton optionsButton = createTextButton("OPTIONS", 440);
        JButton exitButton = createTextButton("EXIT", 540);

        add(playButton);
        add(optionsButton);
        add(exitButton);
    }

    private void loadCustomFont(String path, float size) {
        try (InputStream is = getClass().getResourceAsStream("/ui/fonts/Kranky-Regular.ttf")){
                customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            } catch (Exception e) {
                System.err.println("Font load failed. Using default.");
                customFont = new Font("Serif", Font.PLAIN, (int) size);
        }
    }

    private JButton createTextButton(String text, int y) {
        JButton btn = new JButton(text);

        btn.setBounds(200, y, 300, 60);
        btn.setFont(customFont.deriveFont(50f));
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