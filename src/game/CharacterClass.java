package game;

import characters.Archer;
import characters.Character;
import characters.Knight;
import characters.Ninja;
import characters.Orc;
import characters.Wizard;

public enum CharacterClass {
    KNIGHT("Knight", Archetype.STANDARD),
    WIZARD("Wizard", Archetype.LIGHT),
    ARCHER("Archer", Archetype.LIGHT),
    NINJA("Ninja", Archetype.LIGHT),
    ORC("Orc", Archetype.TANK);

    private final String label;
    private final Archetype archetype;

    CharacterClass(String label, Archetype archetype) {
        this.label = label;
        this.archetype = archetype;
    }

    public String getLabel() {
        return label;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public Character create(String name, int playerId) {
        switch (this) {
            case KNIGHT:
                return new Knight(name, playerId);
            case WIZARD:
                return new Wizard(name, playerId);
            case ARCHER:
                return new Archer(name, playerId);
            case NINJA:
                return new Ninja(name, playerId);
            case ORC:
                return new Orc(name, playerId);
            default:
                throw new IllegalStateException("Unknown class: " + this);
        }
    }

    /** The default name shown for a pick, e.g. "P1 Knight". */
    public String defaultName(int playerId) {
        return "P" + playerId + " " + label;
    }
}
