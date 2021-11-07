package common.map.fixtures.resources;

import java.util.function.Consumer;

import common.map.IFixture;
import common.map.fixtures.MineralFixture;

/**
 * A vein of a mineral.
 */
public class MineralVein implements HarvestableFixture, MineralFixture {
	public MineralVein(String kind, boolean exposed, int dc, int id) {
		this.kind = kind;
		this.exposed = exposed;
		this.dc = dc;
		this.id = id;
	}

	/**
	 * What kind of mineral this is a vein of
	 */
	private final String kind;

	/**
	 * What kind of mineral this is a vein of
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Whether the vein is exposed or not.
	 * TODO: Make non-variable, and instead make a copy-with-modification
	 * and make callers remove the non-exposed and add the exposed version
	 * back.
	 */
	private boolean exposed;

	/**
	 * Whether the vein is exposed or not.
	 * TODO: Make non-variable, and instead make a copy-with-modification
	 * and make callers remove the non-exposed and add the exposed version
	 * back.
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * Set whether the vein is exposed or not.
	 * TODO: Make non-variable, and instead make a copy-with-modification
	 * and make callers remove the non-exposed and add the exposed version
	 * back.
	 */
	public void setExposed(boolean exposed) {
		this.exposed = exposed;
	}

	/**
	 * The DC to discover the vein.
	 *
	 * TODO: Provide good default
	 */
	private final int dc;

	/**
	 * The DC to discover the vein.
	 *
	 * TODO: Provide good default
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * The ID number.
	 */
	private final int id;

	/**
	 * The ID number.
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
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public MineralVein copy(boolean zero) {
		final MineralVein retval = new MineralVein(kind, exposed, (zero) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof MineralVein) {
			return kind.equals(((MineralVein) fixture).getKind()) &&
				exposed == ((MineralVein) fixture).isExposed();
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MineralVein && equalsIgnoringID((MineralVein) obj)) {
			return ((MineralVein) obj).getId() == id;
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
		if (exposed) {
			return String.format("A %s deposit, exposed, DC %d", kind, dc);
		} else {
			return String.format("A %s deposit, not exposed, DC %d", kind, dc);
		}
	}

	@Override
	public String getDefaultImage() {
		return "mineral.png";
	}

	@Override
	public String getPlural() {
		return "Mineral veins";
	}

	@Override
	public String getShortDescription() {
		return (exposed) ? "exposed " + kind : "unexposed " + kind;
	}
}
