package model.map.events;



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
	 * @param tName
	 *            the name of the town, fortress, or city
	 * @param idNum the ID number.
	 */
	public TownEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final long idNum) {
		super(EventKind.Town, tStatus, tSize, tName);
		dc = discdc;
		id = idNum;
	}

	/**
	 * The DC to discover the town. TODO: Should perhaps be mutable.
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
	 * @return an XML representation of the event
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<town status=\"").append(status().toString())
				.append("\" size=\"").append(size().toString())
				.append("\" dc=\"").append(dc);
		if (!name().isEmpty()) {
			sbuild.append("\" name=\"");
			sbuild.append(name());
		}
		sbuild.append("\" id=\"");
		sbuild.append(id);
		return sbuild.append("\" />").toString();
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
