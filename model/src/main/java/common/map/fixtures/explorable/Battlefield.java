package common.map.fixtures.explorable;

import common.map.IFixture;

/**
 * "There are the signs of a long-ago battle here"
 */
public class Battlefield implements ExplorableFixture {
	public Battlefield(final int dc, final int id) {
		this.dc = dc;
		this.id = id;
	}

	/**
	 * The required Perception check result to discover the battlefield.
	 */
	private final int dc;

	/**
	 * The required Perception check result to discover the battlefield.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * A unique ID.
	 */
	private final int id;

	/**
	 * A unique ID.
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
	public Battlefield copy(final boolean zero) {
		final Battlefield retval = new Battlefield((zero) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Battlefield) {
			return ((Battlefield) obj).getId() == id;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * If we ignore ID, all Battlefields are equal.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		return fixture instanceof Battlefield;
	}

	@Override
	public String getDefaultImage() {
		return "battlefield.png";
	}

	@Override
	public String getPlural() {
		return "Battlefields";
	}

	@Override
	public String getShortDescription() {
		return "signs of a long-ago battle";
	}

	@Override
	public String toString() {
		return "An ancient battlefield with DC " + dc;
	}
}
