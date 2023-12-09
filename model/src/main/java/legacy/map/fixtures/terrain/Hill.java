package legacy.map.fixtures.terrain;

import legacy.map.fixtures.TerrainFixture;
import legacy.map.IFixture;
import legacy.map.HasMutableImage;

/**
 * A hill on the map. Should increase unit's effective vision by a small
 * fraction when the unit is on it, if not in forest.
 *
 * TODO: Implement that
 *
 * TODO: Convert to a boolean property of the tile instead of a fixture, like mountains. Start by removing ID.
 */
public class Hill implements TerrainFixture, HasMutableImage {
    public Hill(final int id) {
        this.id = id;
    }

    /**
     * The ID number of this fixture.
     */
    private final int id;

    /**
     * The ID number of this fixture.
     */
    @Override
    public int getId() {
        return id;
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
     * The filename of an image to use as an icon for this instance.
     */
    @Override
    public void setImage(final String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Hill";
    }

    @Override
    public String getDefaultImage() {
        return "hill.png";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final Hill it) {
            return it.getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equalsIgnoringID(final IFixture fixture) {
        return fixture instanceof Hill;
    }

    @Override
    public String getPlural() {
        return "Hills";
    }

    @Override
    public String getShortDescription() {
        return "a hill";
    }

    @Override
    public Hill copy(final CopyBehavior zero) {
        final Hill retval = new Hill(id);
        retval.setImage(image);
        return retval;
    }

    @Override
    public int getDC() {
        return 10;
    }
}
