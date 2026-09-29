package game;

import characters.Character;

import java.util.EnumMap;
import java.util.Map;

/**
 * The one place sprites are registered.
 *
 * This used to be duplicated: the battle screen kept its own string-keyed map
 * and the select screen kept another, so a character could be added to one and
 * silently missing from the other (which is exactly how the Orc ended up
 * invisible in battle). Keying off the CharacterClass enum and building every
 * entry up front means a gap is reported at startup instead of rendering
 * nothing at all.
 */
final class Sprites {

    private static final Map<CharacterClass, CharacterSprite> CACHE = new EnumMap<>(CharacterClass.class);

    static {
        for (CharacterClass id : CharacterClass.values()) {
            CharacterSprite sprite = build(id);
            if (sprite == null) {
                System.err.println("[sprite] No sprite is defined for CharacterClass." + id
                        + " - add a case to game.Sprites.build(). A labelled placeholder "
                        + "will be drawn in its place.");
            } else {
                CACHE.put(id, sprite);
            }
        }
    }

    private Sprites() {
    }

    /**
     * Sprites are generated in code rather than loaded from disk, so there is no
     * file path to get wrong; a missing entry can only mean an unhandled enum case.
     */
    private static CharacterSprite build(CharacterClass id) {
        try {
            switch (id) {
                case KNIGHT: return SpriteFactory.knight();
                case WIZARD: return SpriteFactory.wizard();
                case ARCHER: return SpriteFactory.archer();
                case NINJA: return SpriteFactory.ninja();
                case ORC: return SpriteFactory.orc();
                default: return null;
            }
        } catch (RuntimeException e) {
            System.err.println("[sprite] Failed to build the sprite for CharacterClass." + id + ": " + e);
            e.printStackTrace();
            return null;
        }
    }

    /** @return the sprite, or null if none could be built (caller draws a placeholder). */
    static CharacterSprite forClass(CharacterClass id) {
        return CACHE.get(id);
    }

    static CharacterSprite forCharacter(Character c) {
        return forClass(c.getCharacterClass());
    }
}
