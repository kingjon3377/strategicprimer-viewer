package legacy.map.fixtures.resources;

import legacy.map.IFixture;
import legacy.map.fixtures.MineralFixture;

/**
 * A vein of a mineral.
 */
public final class MineralVein implements HarvestableFixture, MineralFixture {
	public MineralVein(final String kind, final ExposureStatus exposure, final int dc, final int id) {
		this.kind = kind;
		this.exposure = exposure;
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
	private ExposureStatus exposure;

	/**
	 * Whether the vein is exposed or not.
	 * TODO: Make non-variable, and instead make a copy-with-modification
	 * and make callers remove the non-exposed and add the exposed version
	 * back.
	 */
	public ExposureStatus getExposure() {
		return exposure;
	}

	/**
	 * Set whether the vein is exposed or not.
	 * TODO: Make non-variable, and instead make a copy-with-modification
	 * and make callers remove the non-exposed and add the exposed version
	 * back.
	 */
	public void setExposure(final ExposureStatus exposure) {
		this.exposure = exposure;
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
	public void setImage(final String image) {
		this.image = image;
	}

	@Override
	public MineralVein copy(final CopyBehavior zero) {
		final MineralVein retval = new MineralVein(kind, exposure, (zero == CopyBehavior.ZERO) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final MineralVein it) {
			return kind.equals(it.getKind()) &&
					exposure == it.getExposure();
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final MineralVein it && equalsIgnoringID(it)) {
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
	public String toString() {
		return (switch (exposure) {
			case EXPOSED -> "A %s deposit, exposed, DC %d";
			case HIDDEN -> "A %s deposit, not exposed, DC %d";
		}).formatted(kind, dc);
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
		return (switch (exposure) {
			case EXPOSED -> "exposed ";
			case HIDDEN -> "unexposed ";
		}) + kind;
	}
}
