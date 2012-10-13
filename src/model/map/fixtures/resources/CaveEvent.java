package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.events.IEvent;

/**
 * "There are extensive caves beneath this tile".
 *
 * @author Jonathan Lovelace
 *
 */
public final class CaveEvent implements IEvent,
		HasImage, HarvestableFixture {
	/**
	 * Constructor.
	 *
	 * @param discdc the DC to discover the caves
	 * @param idNum the ID number for the event.
	 */
	public CaveEvent(final int discdc, final int idNum) {
		super();
		dc = discdc;
		id = idNum;
	}

	/**
	 * The DC to discover the caves. TODO: Should perhaps be mutable.
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
		return "There are extensive caves beneath this tile.";
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical CaveEvent.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof CaveEvent && ((TileFixture) obj).getID() == id);
	}

	/**
	 *
	 * @return a hash value for the event. Constant, as our only state is DC,
	 *         and that's zeroed in players' maps.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 *
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "Caves with DC " + dc;
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
	 * The event's ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return the event's ID number.
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof CaveEvent;
	}

	/**
	 * Image from OpenGameArt.org, by user MrBeast, from page
	 * http://opengameart.org/content/cave-tileset-0 .
	 *
	 * @return the name of the image representing a cave
	 */
	@Override
	public String getImage() {
		return "cave.png";
	}
}
