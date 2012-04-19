package model.map.events;

import model.map.TileFixture;


/**
 * "There are extensive caves beneath this tile".
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class CaveEvent implements IEvent {
	/**
	 * Constructor.
	 * 
	 * @param discdc
	 *            the DC to discover the caves
	 */
	public CaveEvent(final int discdc) {
		super();
		dc = discdc;
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
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical CaveEvent.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof CaveEvent && ((CaveEvent) obj).dc == dc);
	}

	/**
	 * 
	 * @return a hash value for the event.
	 */
	@Override
	public int hashCode() {
		return dc;
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
	 * @return an XML representation of the event.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<cave dc=\"").append(dc).append("\" />").toString();
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
