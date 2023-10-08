package common.map.fixtures;

import common.map.IFixture;
import common.map.TileFixture;
import common.map.HasMutableImage;

/**
 * A Fixture to encapsulate arbitrary text associated with a tile, so we can
 * improve the interface, have more than one set of text per tile, and be clear
 * on <em>which turn</em> encounters happened.
 */
public final class TextFixture implements TileFixture, HasMutableImage {
    public TextFixture(final String text, final int turn) {
        this.text = text;
        this.turn = turn;
    }

    /**
     * The text.
     */
    private final String text;

    /**
     * The text.
     */
    public String getText() {
        return text;
    }

    /**
     * The turn it's associated with.
     */
    private final int turn;

    /**
     * The turn it's associated with.
     */
    public int getTurn() {
        return turn;
    }

    /**
     * The filename of an image to use as an icon for this instance.
     */
    private String image = "";

    /**
     * The filename of an image to use as an icon for this instance.
     */
    @Override
    public String getImage() {
        return image;
    }

    /**
     * Set the filename of an image to use as an icon for this instance.
     */
    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    /**
     * Clone the object.
     */
    @Override
    public TextFixture copy(final CopyBehavior zero) {
        final TextFixture retval = new TextFixture(text, turn);
        retval.setImage(image);
        return retval;
    }

    @Override
    public String getShortDescription() {
        return (turn == -1) ? text : String.format("%s (turn %d)", text, turn);
    }

    @Override
    public String toString() {
        return getShortDescription();
    }

    @Override
    public String getDefaultImage() {
        return "text.png";
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof TextFixture tf) {
            return tf.getText().equals(text) && tf.turn == turn;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return text.hashCode() << turn;
    }

    /**
     * TextFixtures deliberately don't have a UID, unlike fixtures that
     * used to not have them because there were <em>so many</em> in the world map.
     */
    @Override
    public int getId() {
        return -1;
    }

    /**
     * Since text fixtures don't have an ID, this can simply delegate to equals()
     */
    @Override
    public boolean equalsIgnoringID(final IFixture fixture) {
        return equals(fixture);
    }

    @Override
    public String getPlural() {
        return "Arbitrary-text notes";
    }

    /**
     * The required Perception check result for an explorer to find the note.
     */
    @Override
    public int getDC() {
        return 5;
    }

    /**
     * A TextFixture is a note *to a player* that there's no other fixture
     * to represent, and so shouldn't be in the main map.
     */
    @Override
    public boolean subsetShouldSkip() {
        return true;
    }
}
