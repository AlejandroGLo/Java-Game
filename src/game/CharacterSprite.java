package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Map;

/**
 * One class's pixel art, split into independently posable body parts.
 *
 * The art is authored as a single grid in SpriteFactory; this splits that grid
 * into head / torso / two arms / two legs so the renderer can swing limbs
 * instead of drawing one frozen image. Each part keeps its original position in
 * the grid, so re-assembling them at zero rotation reproduces the original
 * sprite exactly.
 */
class CharacterSprite {

    private enum Part {HEAD, TORSO, BACK_ARM, FRONT_ARM, BACK_LEG, FRONT_LEG}

    private static final int COLS = SpriteFactory.COLS;
    private static final int ROWS = SpriteFactory.ROWS;

    /** Limbs rotate about these grid coordinates. */
    private static final double SHOULDER_ROW = 6.5;
    private static final double BACK_ARM_COL = 2.5;
    private static final double FRONT_ARM_COL = 12.0;
    private static final double BACK_LEG_COL = 5.5;
    private static final double FRONT_LEG_COL = 9.5;

    private final PixelSprite head, torso, backArm, frontArm, backLeg, frontLeg;
    private final PixelSprite headFlash, torsoFlash, backArmFlash, frontArmFlash, backLegFlash, frontLegFlash;
    private final double hipRow;

    private CharacterSprite(char[][] grid, Map<Character, Color> palette, int legTopRow) {
        this.hipRow = legTopRow;
        head = part(grid, palette, legTopRow, Part.HEAD);
        torso = part(grid, palette, legTopRow, Part.TORSO);
        backArm = part(grid, palette, legTopRow, Part.BACK_ARM);
        frontArm = part(grid, palette, legTopRow, Part.FRONT_ARM);
        backLeg = part(grid, palette, legTopRow, Part.BACK_LEG);
        frontLeg = part(grid, palette, legTopRow, Part.FRONT_LEG);

        headFlash = head.silhouette();
        torsoFlash = torso.silhouette();
        backArmFlash = backArm.silhouette();
        frontArmFlash = frontArm.silhouette();
        backLegFlash = backLeg.silhouette();
        frontLegFlash = frontLeg.silhouette();
    }

    /**
     * @param legTopRow first row treated as legs. Robed classes pass a low value
     *                  so the robe stays with the torso and only the feet move.
     */
    static CharacterSprite fromGrid(char[][] grid, Map<Character, Color> palette, int legTopRow) {
        return new CharacterSprite(grid, palette, legTopRow);
    }

    private static PixelSprite part(char[][] grid, Map<Character, Color> palette, int legTopRow, Part want) {
        boolean[][] mask = new boolean[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                mask[r][c] = regionOf(r, c, legTopRow) == want;
            }
        }
        return new PixelSprite(PixelSprite.bake(grid, palette, mask));
    }

    /**
     * Which body part owns a cell. Weapons live in the outer columns and are
     * assigned to the front arm so they move with the hand; the row limit keeps
     * a flared robe from being dragged along with the arms.
     */
    private static Part regionOf(int r, int c, int legTopRow) {
        if (c >= 12 && r <= 16) return Part.FRONT_ARM;
        if (c <= 2 && r >= 4 && r <= 16) return Part.BACK_ARM;
        if (r <= 5) return Part.HEAD;
        if (r >= legTopRow) return c <= 7 ? Part.BACK_LEG : Part.FRONT_LEG;
        return Part.TORSO;
    }

    void drawPosed(Graphics2D g, double x, double y, int width, int height, boolean facingRight, Animator a) {
        double cellW = width / (double) COLS;
        double cellH = height / (double) ROWS;

        // Anchor at the feet so squash and stretch push against the ground.
        AffineTransform base = new AffineTransform();
        base.translate(x + width / 2.0, y + height);
        base.scale((facingRight ? 1 : -1) * cellW * a.scaleX, cellH * a.scaleY);
        if (a.leanDeg != 0) {
            base.rotate(Math.toRadians(a.leanDeg));
        }
        base.translate(-COLS / 2.0, -ROWS);
        base.translate(0, a.bodyOffsetY);

        // Back limbs first so the body overlaps them, then front limbs on top.
        drawLimb(g, base, backArm, backArmFlash, BACK_ARM_COL, SHOULDER_ROW, a.backArmDeg, a.flashAlpha);
        drawLimb(g, base, backLeg, backLegFlash, BACK_LEG_COL, hipRow, a.backLegDeg, a.flashAlpha);
        drawLimb(g, base, torso, torsoFlash, 0, 0, 0, a.flashAlpha);
        drawLimb(g, base, head, headFlash, 0, 0, 0, a.flashAlpha);
        drawLimb(g, base, frontLeg, frontLegFlash, FRONT_LEG_COL, hipRow, a.frontLegDeg, a.flashAlpha);
        drawLimb(g, base, frontArm, frontArmFlash, FRONT_ARM_COL, SHOULDER_ROW, a.frontArmDeg, a.flashAlpha);
    }

    private void drawLimb(Graphics2D g, AffineTransform base, PixelSprite sprite, PixelSprite flash,
                          double pivotCol, double pivotRow, double angleDeg, double flashAlpha) {
        AffineTransform t = new AffineTransform(base);
        if (angleDeg != 0) {
            t.translate(pivotCol, pivotRow);
            t.rotate(Math.toRadians(angleDeg));
            t.translate(-pivotCol, -pivotRow);
        }
        sprite.draw(g, t, 1);
        if (flashAlpha > 0) {
            flash.draw(g, t, flashAlpha * 0.85);
        }
    }
}
