package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

class MyFrame extends JFrame{

    JLabel l;
    JButton b1,b2,b3,b4,b5,b6;

    MyFrame(){
        super("Battle Game");

        b1 = new JButton("TRIBATTLE");
        b1.setPreferredSize(new Dimension(0,150));
        add(b1, BorderLayout.NORTH); 

        b2 = new JButton("Two");
        b2.setPreferredSize(new Dimension(0,100));
        add(b2, BorderLayout.SOUTH);  

        b3 = new JButton("Three");
        add(b3, BorderLayout.WEST); 

        b4 = new JButton("Four");  
        add(b4, BorderLayout.EAST);   
        
        Panel p = new Panel();
        p.setLayout(new GridLayout(3,1));
        p.add(new Button("mon"));
        p.add(new Button("mon"));
        p.add(new Button("mon"));
        add(p, BorderLayout.CENTER);

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
