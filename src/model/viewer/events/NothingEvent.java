package model.viewer.events;

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
	public boolean equals(final Object obj) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
		return this == obj || obj instanceof NothingEvent;
	}

	/**
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		return "Nothing interesting here ...";
	}
	/**
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A nothing event.";
	}
}
