package legacy.map.fixtures;

import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import org.jetbrains.annotations.NotNull;

/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 */
public class Ground implements MineralFixture, HasMutableImage {
	public Ground(final int id, final String kind, final boolean exposed) {
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
	public @NotNull String getKind() {
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
	public void setExposed(final boolean exposed) {
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
	 *
	 * TODO: Extract 'HasMutableId' interface for this and Forest
	 */
	public void setId(final int id) {
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
	public @NotNull String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final @NotNull String image) {
		this.image = image;
	}

	/**
	 * Clone the object.
	 */
	@Override
	public @NotNull Ground copy(final @NotNull IFixture.CopyBehavior zero) {
		final Ground retval = new Ground(id, kind, exposed);
		retval.setImage(image);
		return retval;
	}

	/**
	 * Default image depends on whether the ground is exposed or not.
	 */
	@Override
	public @NotNull String getDefaultImage() {
		return (exposed) ? "expground.png" : "blank.png";
	}

	/**
	 * An object is equal if it is Ground of the same kind, either both or
	 * neither are exposed, and it has the same ID.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof final Ground that) {
			return kind.equals(that.getKind()) &&
					exposed == that.isExposed() && id == that.getId();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public @NotNull String getShortDescription() {
		return String.format("%s %s ground", (exposed) ? "Exposed" : "Unexposed", kind);
	}

	@Override
	public String toString() {
		return String.format("%s, ID #%d", getShortDescription(), id);
	}

	/**
	 * If we ignore ID, a fixture is equal if it is a Ground with equal
	 * kind and either both or neither are exposed.
	 */
	@Override
	public boolean equalsIgnoringID(final @NotNull IFixture fixture) {
		if (this == fixture) {
			return true;
		} else if (fixture instanceof final Ground that) {
			return kind.equals(that.getKind()) && exposed == that.isExposed();
		} else {
			return false;
		}
	}

	/**
	 * This works as the plural for our purposes, since it functions as a collective noun.
	 */
	@Override
	public @NotNull String getPlural() {
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
