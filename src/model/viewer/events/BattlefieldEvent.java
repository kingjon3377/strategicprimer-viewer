package model.viewer.events;

/**
 * "There are the signs of a long-ago battle here".
 * 
 * @author Jonathan Lovelace
 */
public final class BattlefieldEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * 
	 * @param discdc
	 *            the DC to discover the battlefield.
	 */
	public BattlefieldEvent(final int discdc) {
		super(EventKind.Battlefield);
		dc = discdc;
	}

	/**
	 * The DC to discover the battlefield. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 * 
	 * @return the DC to discover the event.
	 * 
	 * @see model.viewer.events.AbstractEvent#getDC()
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * @return exploration-result text for the event.
	 *
	 * @see model.viewer.events.AbstractEvent#getText()
	 */
	@Override
	public String getText() {
		return "There are the signs of a long-ago battle here.";
	}

}
