package common.map.fixtures;

import common.map.IFixture;
import common.map.HasMutableImage;

/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 */
public class Ground implements MineralFixture, HasMutableImage {
	public Ground(int id, String kind, boolean exposed) {
		this.id = id;
		this.kind = kind;
		this.exposed = exposed;
	}

	/**
	 * The kind of ground.
	 */
	private final String kind;

	/**
	 * The kind of ground.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Whether the ground is exposed.
	 *
	 * TODO: convert to enum, or make Ground a sealed interface with ExposedGround and UnexposedGround implementations.
	 */
	private boolean exposed;

	/**
	 * Whether the ground is exposed.
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * Set whether the ground is exposed.
	 */
	public void setExposed(boolean exposed) {
		this.exposed = exposed;
	}

	/**
	 * The ID number.
	 *
	 * FIXME: Why is this variable?
	 */
	private int id;

	/**
	 * The ID number.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * Set the ID number.
	 * @deprecated I don't know why this was variable, but I've ported it as such just in case
	 */
	@Deprecated
	public void setId(int id) {
		this.id = id;
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
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * Clone the object.
	 */
	@Override
	public Ground copy(boolean zero) {
		Ground retval = new Ground(id, kind, exposed);
		retval.setImage(image);
		return retval;
	}

	/**
	 * Default image depends on whether the ground is exposed or not.
	 */
	@Override
	public String getDefaultImage() {
		return (exposed) ? "expground.png" : "blank.png";
	}

	/**
	 * An object is equal if it is Ground of the same kind, either both or
	 * neither are exposed, and it has the same ID.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Ground) {
			return kind.equals(((Ground) obj).getKind()) &&
				exposed == ((Ground) obj).isExposed() && id == ((Ground) obj).getId();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String getShortDescription() {
		return String.format("%s %s ground", (exposed) ? "Exposed" : "Unexposed", kind);
	}

	@Override
	public String toString() {
		return String.format("%s, ID #%d", getShortDescription(), id);
	}

	/**
	 * If we ignore ID, a fixture is equal if if it is a Ground with equal
	 * kind and either both or neither are exposed.
	 */
	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof Ground) {
			return kind.equals(((Ground) fixture).getKind()) && exposed == ((Ground) fixture).isExposed();
		} else {
			return false;
		}
	}

	/**
	 * This works as the plural for our purposes, since it functions as a collective noun.
	 */
	@Override
	public String getPlural() {
		return "Ground";
	}

	/**
	 * The required Perception check result for an explorer to find the
	 * fixture. This does not cover digging to deliberately uncover it.
	 */
	@Override
	public int getDC() {
		return (exposed) ? 10 : 40;
	}
}