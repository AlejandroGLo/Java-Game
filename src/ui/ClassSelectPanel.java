package ui;

import characters.Character;
import game.CharacterClass;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClassSelectPanel extends JPanel {
    private static final String[] CLASS_NAMES = {"Knight", "Wizard", "Archer", "Ninja"};

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] p1Combos = new JComboBox[3];
    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] p2Combos = new JComboBox[3];
    private final JTextField[] p1Names = new JTextField[3];
    private final JTextField[] p2Names = new JTextField[3];

    public ClassSelectPanel(Font font, Consumer<List<Character>> onStart, Runnable onBack) {
        setLayout(null);
        setBackground(Color.BLACK);

        JLabel title = new JLabel("CHOOSE YOUR CHAMPIONS");
        title.setFont(font.deriveFont(44f));
        title.setForeground(Color.WHITE);
        title.setBounds(120, 25, 900, 60);
        add(title);

        JLabel p1Header = new JLabel("PLAYER 1");
        p1Header.setFont(font.deriveFont(26f));
        p1Header.setForeground(new Color(120, 180, 255));
        p1Header.setBounds(120, 105, 300, 40);
        add(p1Header);

        JLabel p2Header = new JLabel("PLAYER 2");
        p2Header.setFont(font.deriveFont(26f));
        p2Header.setForeground(new Color(255, 140, 140));
        p2Header.setBounds(680, 105, 300, 40);
        add(p2Header);

        for (int i = 0; i < 3; i++) {
            buildRow(1, i, 120, 165 + i * 130, p1Combos, p1Names);
            buildRow(2, i, 680, 165 + i * 130, p2Combos, p2Names);
        }

        JButton startButton = createTextButton("START BATTLE", font, 480, 600);
        startButton.addActionListener(e -> onStart.accept(buildRoster()));
        add(startButton);

        JButton backButton = createTextButton("BACK", font, 60, 600);
        backButton.addActionListener(e -> onBack.run());
        add(backButton);
    }

    private void buildRow(int playerId, int idx, int x, int y, JComboBox<String>[] combos, JTextField[] names) {
        JLabel label = new JLabel("Character " + (idx + 1) + ":");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setBounds(x, y, 150, 25);
        add(label);

        String defaultClass = CLASS_NAMES[idx % CLASS_NAMES.length];

        JComboBox<String> combo = new JComboBox<>(CLASS_NAMES);
        combo.setSelectedItem(defaultClass);
        combo.setBounds(x, y + 26, 150, 28);
        add(combo);
        combos[idx] = combo;

        JTextField name = new JTextField("P" + playerId + " " + defaultClass);
        name.setBounds(x + 160, y + 26, 190, 28);
        add(name);
        names[idx] = name;

        JLabel desc = new JLabel("<html>" + describeClass(defaultClass) + "</html>");
        desc.setForeground(new Color(200, 200, 200));
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setBounds(x, y + 58, 360, 45);
        add(desc);

        combo.addActionListener(e -> {
            String cls = (String) combo.getSelectedItem();
            desc.setText("<html>" + describeClass(cls) + "</html>");
            String prefix = "P" + playerId + " ";
            if (name.getText().startsWith(prefix) && isClassName(name.getText().substring(prefix.length()))) {
                name.setText(prefix + cls);
            }
        });
    }

    private static boolean isClassName(String s) {
        for (String c : CLASS_NAMES) if (c.equals(s)) return true;
        return false;
    }

    private static String describeClass(String cls) {
        switch (cls) {
            case "Knight":
                return "Primary: Hammer Throw (heavy damage, short splash)<br>Special: Shield Block (halves next hit)";
            case "Wizard":
                return "Primary: Fireball (medium damage, big splash)<br>Special: Blink (teleport forward)";
            case "Archer":
                return "Primary: Arrow Shot (fast, precise)<br>Special: Volley (fires 3 arrows)";
            case "Ninja":
                return "Primary: Throwing Star (fast, low damage)<br>Special: Shadow Dash (quick reposition)";
            default:
                return "";
        }
    }

    private List<Character> buildRoster() {
        List<Character> roster = new ArrayList<>();
        for (int i = 0; i < 3; i++) roster.add(toCharacter(p1Combos[i], p1Names[i], 1));
        for (int i = 0; i < 3; i++) roster.add(toCharacter(p2Combos[i], p2Names[i], 2));
        return roster;
    }

    private Character toCharacter(JComboBox<String> combo, JTextField nameField, int playerId) {
        String cls = (String) combo.getSelectedItem();
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "P" + playerId + " " + cls;
        return CharacterClass.valueOf(cls.toUpperCase()).create(name, playerId);
    }

    private JButton createTextButton(String text, Font font, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 260, 50);
        btn.setFont(font.deriveFont(28f));
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
}
