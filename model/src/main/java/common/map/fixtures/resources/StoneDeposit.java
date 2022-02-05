package common.map.fixtures.resources;

import common.map.IFixture;
import common.map.fixtures.MineralFixture;

/**
 * A deposit (always exposed for now) of stone.
 *
 * TODO: Support non-exposed deposits
 */
public class StoneDeposit implements HarvestableFixture, MineralFixture {
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
	public StoneDeposit copy(final boolean zero) {
		final StoneDeposit retval = new StoneDeposit(stone, (zero) ? 0 : dc, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof StoneDeposit) {
			return ((StoneDeposit) obj).getStone().equals(stone) &&
				((StoneDeposit) obj).getId() == id;
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
		return String.format("A %s deposit, of DC %d", stone.toString(), dc);
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
		if (fixture instanceof StoneDeposit) {
			return ((StoneDeposit) fixture).getStone().equals(stone);
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
		return "an exposed ``stone`` deposit";
	}
}
