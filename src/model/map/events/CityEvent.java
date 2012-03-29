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
	 * @param tName
	 *            the name of the town, fortress, or city
	 */
	public CityEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName) {
		super(EventKind.City, tStatus, tSize, tName);
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
	/**
	 * @return an XML representation of the event.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuilder = new StringBuilder("<city status=\"").append(status().toString())
				.append("\" size=\"").append(size().toString())
				.append("\" dc=\"").append(dc);
		if (!"".equals(name())) {
			sbuilder.append("\" name=\"");
			sbuilder.append(name());
		}
		return sbuilder.append("\" />").toString();
	}

}
