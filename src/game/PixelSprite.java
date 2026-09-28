package game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;

/** A pixel grid baked into an image, drawn through an arbitrary transform. */
class PixelSprite {
    private final BufferedImage image;

    PixelSprite(BufferedImage image) {
        this.image = image;
    }

    PixelSprite(char[][] grid, Map<Character, Color> palette) {
        this(bake(grid, palette, null));
    }

    /**
     * Bakes a grid to an image. When {@code only} is given, every cell whose code
     * is not in that region is left transparent, which is how body parts are split.
     */
    static BufferedImage bake(char[][] grid, Map<Character, Color> palette, boolean[][] mask) {
        int rows = grid.length;
        int cols = grid[0].length;
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mask != null && !mask[r][c]) continue;
                Color color = palette.get(grid[r][c]);
                img.setRGB(c, r, color == null ? 0 : color.getRGB());
            }
        }
        return img;
    }

    /** A copy with every visible pixel turned white, used for the hit flash. */
    PixelSprite silhouette() {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24);
                if (alpha != 0) out.setRGB(x, y, (alpha << 24) | 0xFFFFFF);
            }
        }
        return new PixelSprite(out);
    }

    void draw(Graphics2D g, AffineTransform transform, double alpha) {
        if (alpha <= 0) return;
        Composite old = g.getComposite();
        if (alpha < 1) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
        }
        g.drawImage(image, transform, null);
        g.setComposite(old);
    }
}
