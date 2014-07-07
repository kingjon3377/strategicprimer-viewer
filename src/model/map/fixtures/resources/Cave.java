package model.map.fixtures.resources;

import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * "There are extensive caves beneath this tile".
 *
 * @author Jonathan Lovelace
 *
 */
public final class Cave implements IEvent, HarvestableFixture {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param discdc the DC to discover the caves
	 * @param idNum the ID number for the event.
	 */
	public Cave(final int discdc, final int idNum) {
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Cave
				&& ((TileFixture) obj).getID() == id;
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
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
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
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Cave;
	}

	/**
	 * Image from OpenGameArt.org, by user MrBeast, from page
	 * http://opengameart.org/content/cave-tileset-0 .
	 *
	 * @return the name of the image representing a cave
	 */
	@Override
	public String getDefaultImage() {
		return "cave.png";
	}

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
	 * @return a string describing all caves as a class
	 */
	@Override
	public String plural() {
		return "Caves";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "caves underground";
	}
}
