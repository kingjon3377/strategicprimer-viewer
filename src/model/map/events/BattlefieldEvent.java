package model.map.events;

import model.map.TileFixture;


/**
 * "There are the signs of a long-ago battle here".
 * 
 * @author Jonathan Lovelace
 */
public final class BattlefieldEvent implements IEvent {
	/**
	 * Constructor.
	 * 
	 * @param discdc
	 *            the DC to discover the battlefield.
	 */
	public BattlefieldEvent(final int discdc) {
		super();
		dc = discdc;
	}

	/**
	 * The DC to discover the battlefield. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD
	/**
	 * 
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
		return "There are the signs of a long-ago battle here.";
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical BattlefieldEvent.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof BattlefieldEvent && ((BattlefieldEvent) obj).dc == dc);
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
		return "An ancient battlefield with DC " + dc;
	}
	/**
	 * @return an XML representation of the event.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<battlefield dc=\"").append(dc).append("\" />").toString();
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
		return Integer.valueOf(getZValue()).compareTo(fix.getZValue());
	}
}
