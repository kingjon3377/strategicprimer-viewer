package legacy.map.fixtures.resources;

import legacy.map.IFixture;
import legacy.map.fixtures.MineralFixture;

/**
 * A deposit (always exposed for now) of stone.
 *
 * TODO: Support non-exposed deposits
 */
public final class StoneDeposit implements HarvestableFixture, MineralFixture {
	public StoneDeposit(final StoneKind stone, final int dc, final int id) {
		this.stone = stone;
		this.dc = dc;
		this.id = id;
	}

	/**
	 * What kind of stone this deposit is.
	 */
	private final StoneKind stone;

	/**
	 * What kind of stone this deposit is.
	 */
	public StoneKind getStone() {
		return stone;
	}

	/**
	 * The DC to discover the deposit.
	 *
	 * TODO: Reasonable defaults
	 */
	private final int dc;

	/**
	 * The DC to discover the deposit.
	 *
	 * TODO: Reasonable defaults
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * ID number.
	 */
	private final int id;

	/**
	 * ID number.
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
	public StoneDeposit copy(final CopyBehavior zero) {
		final StoneDeposit retval = new StoneDeposit(stone, (zero == CopyBehavior.ZERO) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final StoneDeposit it) {
			return it.getStone() == stone &&
					it.getId() == id;
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
		return "A %s deposit, of DC %d".formatted(stone, dc);
	}

	/**
	 * The default icon filename.
	 */
	@Override
	public String getDefaultImage() {
		return "stone.png";
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final StoneDeposit it) {
			return it.getStone() == stone;
		} else {
			return false;
		}
	}

	@Override
	public String getKind() {
		return stone.toString();
	}

	@Override
	public String getPlural() {
		return "Stone deposits";
	}

	@Override
	public String getShortDescription() {
		return "an exposed %s deposit".formatted(stone);
	}
}
