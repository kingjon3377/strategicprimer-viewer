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
	 * @param skind
	 *            the kind of stone
	 * @param discdc
	 *            the dc to discover the stone.
	 */
	public StoneEvent(final StoneKind skind, final int discdc) {
		super();
		stone = skind;
		dc = discdc;
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
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| obj instanceof StoneEvent
						&& ((StoneEvent) obj).stone.equals(stone);
	}

	/**
	 * 
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return stone.hashCode();
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
	public String toXML() {
		return new StringBuilder("<stone kind=\"").append(stone.toString())
				.append("\" dc=\"").append(dc).append("\" />").toString();
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
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
}
