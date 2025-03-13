package ui;

import javax.swing.*;
import java.awt.*;


public class MainMenu extends JFrame {
    public MainMenu(){
        setTitle("Tri-Battle");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3,1));

        JButton startButton = new JButton("Start Game");    
        JButton optionsButton = new JButton("Options");    
        JButton exitButton = new JButton("Exit");

        startButton.addActionListener(e -> {
            System.out.println("Starting game...");
            dispose();
        });
        exitButton.addActionListener(e -> System.exit(0));

        add(startButton);
        add(optionsButton);
        add(exitButton);

        setVisible(true);
    } 
}
