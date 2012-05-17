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
			final int discdc, final String tName, final int idNum) {
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
	@Deprecated
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
	private final int id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}
	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}
	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}
	/**
	 * The name of the file this is to be written to.
	 */
	private String file;
}
