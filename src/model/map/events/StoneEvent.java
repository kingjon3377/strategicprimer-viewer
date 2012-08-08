package model.map.events;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A deposit (always exposed for now) of stone.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class StoneEvent implements IEvent, HasImage {
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
	 * @return an XML representation of the event
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<stone kind=\"").append(stone.toString())
				.append("\" dc=\"").append(dc).append("\" id=\"").append(id)
				.append("\" />").toString();
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return this == fix
				|| (fix instanceof StoneEvent && ((StoneEvent) fix).stone
						.equals(stone));
	}

	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}

	/**
	 * The name of the file this is to be written to.
	 */
	private String file;

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		final StoneEvent retval = new StoneEvent(stone(), getDC(), getID());
		retval.setFile(getFile());
		return retval;
	}
}
