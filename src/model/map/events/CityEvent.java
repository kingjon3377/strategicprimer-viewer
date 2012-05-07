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
	 * @param idNum the ID number.
	 */
	public CityEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final long idNum) {
		super(EventKind.City, tStatus, tSize, tName);
		dc = discdc;
		id = idNum;
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
		if (!name().isEmpty()) {
			sbuilder.append("\" name=\"");
			sbuilder.append(name());
		}
		sbuilder.append("\" id=\"");
		sbuilder.append(id);
		return sbuilder.append("\" />").toString();
	}
	/**
	 * ID number.
	 */
	private final long id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public long getID() {
		return id;
	}
}
