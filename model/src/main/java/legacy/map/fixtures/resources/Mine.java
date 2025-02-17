package legacy.map.fixtures.resources;

import legacy.map.IFixture;
import legacy.map.fixtures.MineralFixture;
import common.map.fixtures.towns.TownStatus;

/**
 * A mine---a source of mineral resources.
 */
public final class Mine implements HarvestableFixture, MineralFixture {
	public Mine(final String kind, final TownStatus status, final int id) {
		this.kind = kind;
		this.status = status;
		this.id = id;
	}

	/**
	 * What the mine produces.
	 */
	private final String kind;

	/**
	 * What the mine produces.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The status of the mine.
	 */
	private final TownStatus status;

	/**
	 * The status of the mine.
	 */
	public TownStatus getStatus() {
		return status;
	}

	/**
	 * The ID number of the fixture.
	 */
	private final int id;

	/**
	 * The ID number of the fixture.
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
	 * The default icon filename.
	 */
	@Override
	public String getDefaultImage() {
		return "mine.png";
	}

	@Override
	public String getPlural() {
		return "Mines";
	}

	/**
	 * Clone the object.
	 */
	@Override
	public Mine copy(final CopyBehavior zero) {
		final Mine retval = new Mine(kind, status, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String toString() {
		return "%s mine of %s".formatted(status, kind);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Mine it) {
			return kind.equals(it.getKind()) &&
					status == it.getStatus() &&
					id == it.getId();
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
		if (fixture instanceof final Mine it) {
			return kind.equals(it.getKind()) &&
					status == it.getStatus();
		} else {
			return false;
		}
	}

	@Override
	public String getShortDescription() {
		return "%s %s mine".formatted(status, kind);
	}

	/**
	 * The required Perception check for an explorer to find this fixture.
	 *
	 * TODO: Should perhaps be variable and loaded from XML
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return (TownStatus.Active == status) ? 15 : 25;
	}
}
