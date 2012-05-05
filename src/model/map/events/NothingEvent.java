package model.map.events;

import model.map.TileFixture;


/**
 * "Nothing interesting here...".
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class NothingEvent implements IEvent {
	/**
	 * A singleton to compare with.
	 */
	public static final NothingEvent NOTHING_EVENT = new NothingEvent();
	/**
	 * Constructor.
	 */
	private NothingEvent() {
		super();
	}

	/**
	 * 
	 * 
	 * @return 0: if there's nothing, it's impossible to not find it.
	 */
	@Override
	public int getDC() {
		return 0;
	}

	/**
	 * @return a hash-code for this object.
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's equal to this one---iff it's another NothingEvent.
	 */
	@Override
	public boolean equals(final Object obj) { // $codepro.audit.disable
		return this == obj || obj instanceof NothingEvent;
	}

	/**
	 * 
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return "Nothing interesting here ...";
	}

	/**
	 * 
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A nothing event.";
	}
	/**
	 * @return an XML representation of the event: the empty string.
	 */
	@Override
	public String toXML() {
		return "";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 0;
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
	/**
	 * @return an ID number for the event.
	 */
	@Override
	public long getID() {
		return 0;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return equals(fix);
	}
}
