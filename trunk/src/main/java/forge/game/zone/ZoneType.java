package forge.game.zone;

import java.util.ArrayList;
import java.util.List;

/**
 * The Enum Zone.
 */
public enum ZoneType {

    /** The Hand. */
    Hand(true),

    /** The Library. */
    Library(true),

    /** The Graveyard. */
    Graveyard(false),

    /** The Battlefield. */
    Battlefield(false),

    /** The Exile. */
    Exile(false),

    /** The Command. */
    Command(false),

    /** The Stack. */
    Stack(false),

    Sideboard(true),
    /** Ante. */
    Ante(false);

    public static final ZoneType[] StaticAbilitiesSourceZones = new ZoneType[]{Battlefield, Graveyard, Exile/*, Hand*/};

    private final boolean holdsHiddenInfo;
    private ZoneType(boolean holdsHidden) {
        holdsHiddenInfo = holdsHidden;
    }


    public static ZoneType smartValueOf(final String value) {
        if (value == null) {
            return null;
        }
        if ("All".equals(value)) {
            return null;
        }
        final String valToCompate = value.trim();
        for (final ZoneType v : ZoneType.values()) {
            if (v.name().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }
        throw new IllegalArgumentException("No element named " + value + " in enum Zone");
    }

    public static List<ZoneType> listValueOf(final String values) {
        final List<ZoneType> result = new ArrayList<ZoneType>();
        for (final String s : values.split("[, ]+")) {
            result.add(ZoneType.smartValueOf(s));
        }
        return result;
    }

    public boolean isHidden() {
        return holdsHiddenInfo;
    }

    public boolean isKnown() {
        return !holdsHiddenInfo;
    }
}
