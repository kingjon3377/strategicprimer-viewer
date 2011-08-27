package model.viewer.events;

/**
 * An abandoned, ruined, or burned-out town.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class TownEvent extends AbstractTownEvent {
	/**
	 * Constructor.
	 * 
	 * @param tStatus
	 *            The status of the town
	 * @param tSize
	 *            The size of the town
	 * @param discdc
	 *            The DC to discover it.
	 */
	public TownEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc) {
		super(EventKind.Town, tStatus, tSize);
		dc = discdc;
	}

	/**
	 * The DC to discover the town. TODO: Should perhaps be mutable.
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

}
