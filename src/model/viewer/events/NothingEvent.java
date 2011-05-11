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

}
