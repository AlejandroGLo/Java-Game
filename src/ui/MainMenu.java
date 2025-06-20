package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;

class MyFrame extends JFrame{

    JLabel l;
    JButton b1,b2,b3,b4,b5,b6;

    MyFrame(){
        super("Battle Game");

        ImageIcon backgroundIcon = new ImageIcon("img.jpeg");

        JPanel backgroundPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(backgroundIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        b1 = new JButton("TRIBATTLE");
        b1.setPreferredSize(new Dimension(0,150));
        backgroundPanel.add(b1, BorderLayout.NORTH); 

        b2 = new JButton("Two");
        b2.setPreferredSize(new Dimension(0,100));
        backgroundPanel.add(b2, BorderLayout.SOUTH);  

        b3 = new JButton("Three");
        b3.setPreferredSize(new Dimension(150,0));
        backgroundPanel.add(b3, BorderLayout.WEST); 

        b4 = new JButton("Four");  
        b4.setPreferredSize(new Dimension(150,0));
        backgroundPanel.add(b4, BorderLayout.EAST);   
        
        Panel p = new Panel();
        p.setLayout(new GridLayout(3,1));
        p.add(new Button("mon"));
        p.add(new Button("mon"));
        p.add(new Button("mon"));
        add(p, BorderLayout.CENTER);

        setContentPane(backgroundPanel);
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
