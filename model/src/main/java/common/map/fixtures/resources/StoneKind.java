package common.map.fixtures.resources;

/**
 * The kinds of stone we know about.
 */
public enum StoneKind {
    Limestone("limestone"),
    Marble("marble"),
    Slate("slate"),
    Pumice("pumice"),
    Conglomerate("conglomerate"),
    Sandstone("sandstone"),
    /**
     * Laterite should only be found under jungle.
     */
    Laterite("laterite"),
    Shale("shale");

    StoneKind(final String str) {
        string = str;
    }

    private final String string;

    @Override
    public String toString() {
        return string;
    }

    public static StoneKind parse(final String stone) {
        // TODO: Replace with a HashMap cache?
        for (final StoneKind kind : values()) {
            if (stone.equals(kind.toString())) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Failed to parse StoneKind from " + stone);
    }
}
