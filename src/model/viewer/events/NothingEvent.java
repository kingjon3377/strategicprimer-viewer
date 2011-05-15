package model.viewer.events;

/**
 * "Nothing interesting here...".
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class NothingEvent extends AbstractEvent {
	/**
	 * Constructor.
	 */
	public NothingEvent() {
		super(EventKind.Nothing);
	}

	/**
	 * 
	 * @return 0
	 * 
	 * @see model.viewer.events.AbstractEvent#getDC()
	 */
	@Override
	public int getDC() {
		return 0;
	}
	/**
	 * @return a hash-code for this object.
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one---iff it's another NothingEvent.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || obj instanceof NothingEvent;
	}
}
