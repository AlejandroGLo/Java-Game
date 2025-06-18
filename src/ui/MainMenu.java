package ui;

import java.awt.*;
import javax.swing.*;

class MyFrame extends JFrame{

    Label l;
    Button b1, b2, b3, b4;

    MyFrame(){
        super("Battle Game");
    }


}

public class MainMenu {
    public static void main(String args[]){
        MyFrame mf = new MyFrame();
        mf.setVisible(true);
        mf.setSize(800,800);
        mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    
}
