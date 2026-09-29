package game;

import java.awt.Graphics2D;

/**
 * Draws a character's sprite outside of a battle (character select, menus).
 *
 * Sprite assembly is package-private, so this is the seam the UI layer uses:
 * it takes only a breathing phase and a scale, and owns no game state.
 */
public final class CharacterPreview {

    private CharacterPreview() {
    }

    /**
     * @param breath  seconds of elapsed animation time, already offset per slot
     * @param popScale extra uniform scale, 1.0 at rest (used for the pop-in)
     */
    public static void draw(Graphics2D g, CharacterClass id, int x, int y, int width, int height,
                            double breath, double popScale) {
        double wave = Math.sin(breath * GameConfig.SELECT_BREATH_SPEED);

        Animator pose = new Animator();
        // Volume-preserving squash: taller means slightly narrower.
        pose.scaleY = (1 + wave * GameConfig.SELECT_BREATH_SCALE) * popScale;
        pose.scaleX = (1 - wave * GameConfig.SELECT_BREATH_SCALE * 0.5) * popScale;
        pose.bodyOffsetY = wave * GameConfig.SELECT_BREATH_BOB;

        CharacterSprite sprite = Sprites.forClass(id);
        if (sprite != null) {
            sprite.drawPosed(g, x, y, width, height, true, pose);
        }
    }
}
