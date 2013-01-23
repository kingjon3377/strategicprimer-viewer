package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.events.IEvent;

/**
 * A deposit (always exposed for now) of stone. TODO: Implement HasKind when stone() returns a String rather than an enumerated type
 *
 * @author Jonathan Lovelace
 *
 */
public final class StoneEvent implements IEvent,
		HasImage, HarvestableFixture, HasKind {
	/**
	 * Constructor.
	 *
	 * @param skind the kind of stone
	 * @param discdc the dc to discover the stone.
	 * @param idNum the ID number.
	 */
	public StoneEvent(final StoneKind skind, final int discdc, final int idNum) {
		super();
		stone = skind;
		dc = discdc;
		id = idNum;
	}

	/**
	 * What kind of stone this deposit is.
	 */
	private final StoneKind stone;

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
		final StringBuilder build = new StringBuilder("There is an exposed ");
		build.append(stone.toString());
		build.append(" deposit here.");
		return build.toString();
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || obj instanceof StoneEvent
				&& ((StoneEvent) obj).stone.equals(stone)
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
	public String getImage() {
		return "stone.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
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
				|| (fix instanceof StoneEvent && ((StoneEvent) fix).stone
						.equals(stone));
	}
	/**
	 * @return a string representation of the kind of stone in the deposit
	 */
	@Override
	public String getKind() {
		return stone.toString();
	}
}
