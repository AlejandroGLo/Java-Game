package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;

class MyFrame extends JFrame{

    private BufferedImage backgroundImage;
    private Font customFont;

    MyFrame(){
        //Load Font
        loadCustomFont("/fonts/Kranky-Regular.ttf", 32f);

        //Load Image
        try{
            backgroundImage = ImageIO.read(new File("background.jpg"));
        } catch (Exception e){
            e.printStackTrace();
        }

        //Setting Layout Manager
        setContentPane(new BackgroundPanel());
        setLayout(null);

        //Create Panel for Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridBagLayout());

        JButton playButton = createTextButton("PLAY", 200);
        JButton optionsButton = createTextButton("OPTIONS", 270);
        JButton exitButton = createTextButton("EXIT", 340);

        add(playButton);
        add(optionsButton);
        add(exitButton);

    }

    private void loadCustomFont(String path, float size){
        try{
            File fontfile = new File(path);
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontfile).deriveFont(size);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e){
            customFont = new Font("Serif", Font.PLAIN, (int) size);
        }
    }

    private JButton createTextButton(String text, int y){
        JButton btn = new JButton(text);
        int x = (int) (getWidth() * 0.2);
        btn.setBounds(200, y, 200, 40);
        btn.setFont(customFont);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt){
                btn.setForeground(Color.YELLOW);
            } 

            public void mouseExited(java.awt.event.MouseEvent evt){
                btn.setForeground(Color.YELLOW);
            }
        });

        return btn;

    }

    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if (backgroundImage != null){
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}

public class MainMenu {
    public static void main(String args[]){
        MyFrame mf = new MyFrame();
        mf.setVisible(true);
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mf.setExtendedState(JFrame.MAXIMIZED_BOTH);

    }
    
}
