package model.map.events;


/**
 * "Nothing interesting here...".
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class NothingEvent extends AbstractEvent {
	/**
	 * A singleton to compare with.
	 */
	public static final NothingEvent NOTHING_EVENT = new NothingEvent();

	/**
	 * Constructor.
	 */
	private NothingEvent() {
		super(EventKind.Nothing);
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
}
