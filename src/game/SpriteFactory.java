package game;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds small pixel-art grids (15x23) for each class. Coordinates are laid out
 * so a right-facing humanoid stands with its head at rows 0-5, torso/arms at
 * rows 6-14, and legs (or robe) at rows 15-22. PixelSprite scales the result
 * up with nearest-neighbor sampling to keep the blocky pixel look crisp.
 */
final class SpriteFactory {
    static final int COLS = 15;
    static final int ROWS = 23;

    private SpriteFactory() {
    }

    private static char[][] blankGrid() {
        char[][] g = new char[ROWS][COLS];
        for (char[] row : g) Arrays.fill(row, '.');
        return g;
    }

    private static void rect(char[][] g, int r0, int r1, int c0, int c1, char ch) {
        for (int r = Math.max(0, r0); r <= Math.min(ROWS - 1, r1); r++) {
            for (int c = Math.max(0, c0); c <= Math.min(COLS - 1, c1); c++) {
                g[r][c] = ch;
            }
        }
    }

    private static void px(char[][] g, int r, int c, char ch) {
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) g[r][c] = ch;
    }

    /** Shared torso/arms block used by the three humanoid (non-robed) classes. */
    private static char[][] humanoidLimbs() {
        char[][] g = blankGrid();
        rect(g, 6, 14, 1, 2, 'A');   // back arm
        rect(g, 6, 14, 3, 11, 'B');  // torso
        rect(g, 6, 14, 12, 13, 'A'); // front arm
        return g;
    }

    private static void legs(char[][] g, char legColor, char bootColor) {
        rect(g, 15, 21, 4, 6, legColor);
        rect(g, 15, 21, 8, 10, legColor);
        rect(g, 22, 22, 3, 6, bootColor);
        rect(g, 22, 22, 8, 11, bootColor);
    }

    static CharacterSprite knight() {
        char[][] g = humanoidLimbs();
        rect(g, 0, 5, 4, 10, 'H');   // helmet
        rect(g, 0, 0, 6, 8, 'P');    // plume crest
        rect(g, 3, 3, 5, 9, 'V');    // visor slit
        legs(g, 'L', 'D');
        rect(g, 1, 5, 13, 13, 'K');   // blade outline (above the arm)
        rect(g, 1, 12, 14, 14, 'W');  // sword blade
        rect(g, 12, 13, 13, 14, 'X'); // hilt

        Map<Character, Color> palette = new HashMap<>();
        palette.put('H', new Color(195, 199, 209));
        palette.put('P', new Color(196, 44, 44));
        palette.put('V', new Color(35, 35, 42));
        palette.put('B', new Color(172, 177, 191));
        palette.put('A', new Color(150, 155, 170));
        palette.put('L', new Color(92, 97, 112));
        palette.put('D', new Color(58, 58, 68));
        palette.put('K', new Color(45, 45, 52));
        palette.put('W', new Color(210, 215, 225));
        palette.put('X', new Color(122, 82, 46));
        return CharacterSprite.fromGrid(g, palette, 15);
    }

    static CharacterSprite wizard() {
        char[][] g = blankGrid();
        px(g, 0, 7, 'P');
        rect(g, 1, 1, 6, 8, 'P');
        rect(g, 2, 2, 5, 9, 'P');
        rect(g, 3, 3, 4, 10, 'M');   // hat brim
        rect(g, 4, 5, 5, 9, 'F');    // face
        px(g, 4, 6, 'K');
        px(g, 4, 8, 'K');            // eyes

        rect(g, 6, 14, 1, 2, 'A');
        rect(g, 6, 14, 3, 11, 'B');
        rect(g, 6, 14, 12, 13, 'A');

        rect(g, 15, 16, 3, 11, 'B'); // robe
        rect(g, 17, 19, 2, 12, 'B'); // robe flare
        rect(g, 20, 22, 4, 6, 'Z');  // shoe
        rect(g, 20, 22, 8, 10, 'Z'); // shoe

        rect(g, 2, 16, 14, 14, 'S'); // staff shaft
        rect(g, 0, 1, 13, 14, 'G');  // glowing orb

        Map<Character, Color> palette = new HashMap<>();
        palette.put('P', new Color(96, 48, 160));
        palette.put('M', new Color(58, 26, 100));
        palette.put('F', new Color(230, 190, 150));
        palette.put('K', new Color(20, 20, 24));
        palette.put('A', new Color(96, 48, 160));
        palette.put('B', new Color(120, 66, 190));
        palette.put('Z', new Color(60, 40, 30));
        palette.put('S', new Color(110, 74, 42));
        palette.put('G', new Color(255, 221, 90));
        return CharacterSprite.fromGrid(g, palette, 20);
    }

    static CharacterSprite archer() {
        char[][] g = humanoidLimbs();
        rect(g, 0, 5, 4, 10, 'H');   // hood
        rect(g, 5, 5, 3, 11, 'H');   // hood brim
        rect(g, 3, 4, 5, 9, 'F');    // shadowed face
        px(g, 3, 6, 'K');
        px(g, 3, 8, 'K');            // eyes
        rect(g, 4, 9, 0, 1, 'Q');    // quiver
        rect(g, 10, 10, 3, 11, 'X'); // belt
        legs(g, 'L', 'D');

        px(g, 2, 13, 'W');
        rect(g, 3, 13, 14, 14, 'W');
        px(g, 14, 13, 'W');          // bow arc

        Map<Character, Color> palette = new HashMap<>();
        palette.put('H', new Color(64, 130, 74));
        palette.put('F', new Color(70, 55, 45));
        palette.put('K', new Color(230, 230, 230));
        palette.put('Q', new Color(92, 56, 26));
        palette.put('B', new Color(80, 140, 90));
        palette.put('A', new Color(110, 74, 42));
        palette.put('X', new Color(90, 60, 35));
        palette.put('L', new Color(96, 72, 50));
        palette.put('D', new Color(58, 44, 32));
        palette.put('W', new Color(206, 168, 104));
        return CharacterSprite.fromGrid(g, palette, 15);
    }

    static CharacterSprite ninja() {
        char[][] g = humanoidLimbs();
        rect(g, 0, 5, 4, 10, 'H');   // mask
        px(g, 3, 6, 'K');
        px(g, 3, 8, 'K');            // eyes
        rect(g, 5, 6, 10, 11, 'X');  // scarf
        rect(g, 10, 10, 3, 11, 'X'); // sash
        legs(g, 'L', 'D');

        px(g, 8, 13, 'W');
        px(g, 9, 12, 'W');
        px(g, 9, 13, 'W');
        px(g, 9, 14, 'W');
        px(g, 10, 13, 'W');          // shuriken

        Map<Character, Color> palette = new HashMap<>();
        palette.put('H', new Color(45, 45, 50));
        palette.put('K', new Color(210, 40, 40));
        palette.put('X', new Color(150, 30, 30));
        palette.put('B', new Color(38, 38, 42));
        palette.put('A', new Color(48, 48, 53));
        palette.put('L', new Color(38, 38, 42));
        palette.put('D', new Color(20, 20, 22));
        palette.put('W', new Color(210, 210, 220));
        return CharacterSprite.fromGrid(g, palette, 15);
    }
}
