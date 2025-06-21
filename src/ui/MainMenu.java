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

    JLabel l;
    JButton b1,b2,b3,b4,b5,b6;

    MyFrame(){
        //Load Font
        loadCustomFont("/fonts/Kranky-Regular.ttf", 22f);

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

        JButton playButton = new JButton("PLAY");
        JButton optionsButton = new JButton("OPTIONS");
        JButton exitButton = new JButton("EXIT");

        for(JButton btn : new JButton[] {playButton, optionsButton, exitButton}) {
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setForeground(Color.WHITE);
            bnt.set
        }

    }

    private void loadCustomFont(String path, float size){
        try{
            InputStream is = getClass()
        }
    }

    class BackgroundPanel extends JPanel{

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
