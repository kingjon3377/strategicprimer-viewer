package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A deposit (always exposed for now) of stone.
 *
 * @author Jonathan Lovelace
 *
 */
public final class StoneDeposit implements IEvent, HasImage,
		HarvestableFixture, HasKind {
	/**
	 * Constructor.
	 *
	 * @param skind the kind of stone
	 * @param discdc the dc to discover the stone.
	 * @param idNum the ID number.
	 */
	public StoneDeposit(final StoneKind skind, final int discdc, final int idNum) {
		super();
		stone = skind;
		dc = discdc;
		id = idNum;
	}

	/**
	 * What kind of stone this deposit is.
	 */
	private StoneKind stone;

	/**
	 *
	 * @return what kind of stone this deposit is.
	 */
	public StoneKind stone() {
		return stone;
	}

	/**
	 * The DC to discover the stone. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 *
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 *
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final String stoneStr = stone.toString();
		final StringBuilder build = new StringBuilder(40 + stoneStr.length())
				.append("There is an exposed ");
		build.append(stoneStr);
		build.append(" deposit here.");
		final String retval = build.toString();
		assert retval != null;
		return retval;
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof StoneDeposit
				&& ((StoneDeposit) obj).stone.equals(stone)
				&& ((TileFixture) obj).getID() == id;
	}

	/**
	 *
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 *
	 * @return a string representation of the object
	 */
	@Override
	public String toString() {
		return "A " + stone.toString() + " deposit, of DC " + dc;
	}

	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getDefaultImage() {
		return "stone.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		// ESCA-JAVA0076:
		return 40;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| (fix instanceof StoneDeposit && ((StoneDeposit) fix).stone
						.equals(stone));
	}

	/**
	 * @return a string representation of the kind of stone in the deposit
	 */
	@Override
	public String getKind() {
		return stone.toString();
	}

	/**
	 * TODO: Allow arbitrary-text.
	 *
	 * @param kind the new kind
	 */
	@Override
	public void setKind(final String kind) {
		final StoneKind skind = StoneKind.parseStoneKind(kind);
		stone = skind;
	}

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @return a string describing all stone deposits as a class
	 */
	@Override
	public String plural() {
		return "Stone deposits";
	}
}
