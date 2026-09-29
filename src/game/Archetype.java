package game;

/**
 * Broad role a character fills. Drives the attack/defense multipliers and the
 * length of the character's turn; the per-value numbers live in GameConfig.
 */
public enum Archetype {
    STANDARD("Standard"),
    LIGHT("Light"),
    TANK("Tank");

    private final String label;

    Archetype(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
