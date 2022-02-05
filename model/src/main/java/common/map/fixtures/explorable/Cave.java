package common.map.fixtures.explorable;

import common.map.IFixture;

/**
 * "There are extensive caves beneath this tile".
 */
public class Cave implements ExplorableFixture {
	public Cave(final int dc, final int id) {
		this.dc = dc;
		this.id = id;
	}

	/**
	 * The required Perception check result to discover the caves.
	 */
	private int dc;

	/**
	 * The required Perception check result to discover the caves.
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
	public Cave copy(final boolean zero) {
		Cave retval = new Cave((zero) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Cave) {
			return ((Cave) obj).getId() == id;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "Caves with DC " + dc;
	}

	/**
	 * If we ignore ID (and DC), all caves are equal.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		return fixture instanceof Cave;
	}

	/**
	 * @author MrBeast
	 * @see http://opengameart.org/content/cave-tileset-0
	 */
	@Override
	public String getDefaultImage() {
		return "cave.png";
	}

	@Override
	public String getPlural() {
		return "Caves";
	}

	@Override
	public String getShortDescription() {
		return "caves underground";
	}
}
