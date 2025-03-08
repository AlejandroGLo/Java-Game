import javax.swing.JFrame;

public class Window extends JFrame implements Runnable {

    public Window(int width, int height, String title){
        setSize(width, height);
        setTitle(title);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void run(){

    }

    
}
