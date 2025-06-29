package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

class MyFrame extends JFrame {
    private BufferedImage backgroundImage;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    MyFrame() {
        loadCustomFont("/ui/fonts/Kranky-Regular.ttf", 32f);

        // Load background image
        try (InputStream is = getClass().getResourceAsStream("/ui/img/background.jpeg")) {
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                System.err.println("❌ Background not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up CardLayout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        setContentPane(mainPanel);

        // Create and add panels
        JPanel menuPanel = createMainMenuPanel();
        JPanel optionsPanel = createOptionsPanel();

        mainPanel.add(menuPanel, "menu");
        mainPanel.add(optionsPanel, "options");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void loadCustomFont(String path, float size) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            System.err.println("❌ Font load failed");
            customFont = new Font("Serif", Font.PLAIN, (int) size);
        }
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new BackgroundPanel();
        panel.setLayout(null);

        JLabel title = new JLabel("SHOWDOWN");
        title.setFont(customFont.deriveFont(78f));
        title.setForeground(Color.WHITE);
        title.setBounds(150, 150, 600, 70);
        panel.add(title);

        JButton playButton = createTextButton("PLAY", 340);
        JButton optionsButton = createTextButton("OPTIONS", 440);
        JButton exitButton = createTextButton("EXIT", 540);

        // Add listeners
        playButton.addActionListener(e -> System.out.println("Start Game (not implemented)"));
        optionsButton.addActionListener(e -> cardLayout.show(mainPanel, "options"));
        exitButton.addActionListener(e -> showExitDialog());

        panel.add(playButton);
        panel.add(optionsButton);
        panel.add(exitButton);

        return panel;
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.BLACK);

        JLabel label = new JLabel("OPTIONS");
        label.setFont(customFont.deriveFont(64f));
        label.setForeground(Color.WHITE);
        label.setBounds(150, 150, 400, 70);
        panel.add(label);

        JButton backButton = createTextButton("BACK", 300);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        panel.add(backButton);

        return panel;
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
                btn.setForeground(Color.WHITE);
            }
        });

        return btn;
    }

    private void showExitDialog() {
        int result = JOptionPane.showOptionDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Exit", "Cancel"},
                "Cancel"
        );

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
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
        SwingUtilities.invokeLater(MyFrame::new);
    }
}