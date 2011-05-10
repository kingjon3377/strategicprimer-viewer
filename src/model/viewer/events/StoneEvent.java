package model.viewer.events;
/**
 * A deposit (always exposed for now) of stone.
 * @author Jonathan Lovelace
 *
 */
public final class StoneEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * @param skind the kind of stone
	 * @param discdc the dc to discover the stone.
	 */
	public StoneEvent(final StoneKind skind, final int discdc) {
		super(EventKind.Stone);
		stone = skind;
		dc = discdc;
	}
	/**
	 * The kinds of stone we know about (for purposes of this event).
	 */
	public enum StoneKind {
		Limestone, Marble;
	}
	/**
	 * What kind of stone this deposit is.
	 */
	public final StoneKind stone;
	/**
	 * The DC to discover the stone. TODO: Should perhaps be mutable.
	 */
	public final int dc; // NOPMD

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

}
