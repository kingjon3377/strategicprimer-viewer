package common.map.fixtures.resources;

import java.util.function.Consumer;

import common.map.IFixture;
import common.map.fixtures.MineralFixture;
import common.map.fixtures.towns.TownStatus;

/**
 * A mine---a source of mineral resources.
 */
public class Mine implements HarvestableFixture, MineralFixture {
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
	public Mine copy(final boolean zero) {
		final Mine retval = new Mine(kind, status, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String toString() {
		return String.format("%s mine of %s", status.toString(), kind);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Mine) {
			return kind.equals(((Mine) obj).getKind()) &&
				status.equals(((Mine) obj).getStatus()) &&
				id == ((Mine) obj).getId();
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
		if (fixture instanceof Mine) {
		return kind.equals(((Mine) fixture).getKind()) &&
			status.equals(((Mine) fixture).getStatus());
		} else {
			return false;
		}
	}

	@Override
	public String getShortDescription() {
		return String.format("%s %s mine", status.toString(), kind);
	}

	/**
	 * The required Perception check for an explorer to find this fixture.
	 *
	 * TODO: Should perhaps be variable and loaded from XML
	 */
	@Override
	public int getDC() {
		return (TownStatus.Active.equals(status)) ? 15 : 25;
	}
}
