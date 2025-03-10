import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class Main {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new MainMenu();
        });
    }
}

class MainMenu extends JFrame{
    public MainMenu(){
        setTitle("TriBattle");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("TriBattle Game Menu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);


        JButton exitButton = createMenuButton("Exit");


    }

    private JButton createMenuButton(String text){
        JButton button = new JButton(text);
        return button;
    }

}
