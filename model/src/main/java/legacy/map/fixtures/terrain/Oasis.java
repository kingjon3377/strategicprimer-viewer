package legacy.map.fixtures.terrain;

import legacy.map.fixtures.TerrainFixture;
import legacy.map.IFixture;
import legacy.map.HasMutableImage;

/**
 * An oasis on the map.
 */
public class Oasis implements TerrainFixture, HasMutableImage {
	public Oasis(final int id) {
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
		return "Oasis";
	}

	@Override
	public String getDefaultImage() {
		return "oasis.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Oasis it) {
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
		return fixture instanceof Oasis;
	}

	@Override
	public String getPlural() {
		return "Oases";
	}

	@Override
	public String getShortDescription() {
		return "an oasis";
	}

	@Override
	public Oasis copy(final CopyBehavior zero) {
		final Oasis retval = new Oasis(id);
		retval.setImage(image);
		return retval;
	}

	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return 15;
	}
}
