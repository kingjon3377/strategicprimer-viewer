package model.map.events;

/**
 * An abandoned, ruined, or burned-out city.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class CityEvent extends AbstractTownEvent {
	/**
	 * Constructor.
	 * 
	 * @param tStatus
	 *            The status of the city
	 * @param tSize
	 *            The size of the city
	 * @param discdc
	 *            The DC to discover it.
	 */
	public CityEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc) {
		super(EventKind.City, tStatus, tSize);
		dc = discdc;
	}

	/**
	 * The DC to discover the city. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 * 
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

}
