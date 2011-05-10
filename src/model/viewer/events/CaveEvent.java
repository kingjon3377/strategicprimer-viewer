package model.viewer.events;

/**
 * "There are extensive caves beneath this tile."
 * @author Jonathan Lovelace
 *
 */
public final class CaveEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * @param discdc the DC to discover the caves
	 */
	public CaveEvent(final int discdc) {
		super(EventKind.Caves);
		dc = discdc;
	}
	/**
	 * The DC to discover the caves. TODO: Should perhaps be mutable.
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
