package model.map.events;

import model.map.TileFixture;


/**
 * An abandoned, ruined, or burned-out fortification.
 * 
 * FIXME: We want this to share a tag, and thus probably model code as well,
 * with Fortress.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class FortificationEvent extends AbstractTownEvent {
	/**
	 * Constructor.
	 * 
	 * @param tStatus
	 *            The status of the fortification
	 * @param tSize
	 *            The size of the fortification
	 * @param discdc
	 *            The DC to discover it.
	 * @param tName
	 *            the name of the town, fortress, or city
	 * @param idNum the ID number.
	 */
	public FortificationEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum) {
		super(EventKind.Fortification, tStatus, tSize, tName);
		dc = discdc;
		id = idNum;
	}

	/**
	 * The DC to discover the fortification. TODO: Should perhaps be mutable.
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
		final StringBuilder sbuild = new StringBuilder("<fortification status=\"")
				.append(status().toString()).append("\" size=\"")
				.append(size().toString()).append("\" dc=\"").append(dc);
		if (!name().isEmpty()) {
			sbuild.append("\" name=\"");
			sbuild.append(name());
		}
		sbuild.append("\" id=\"");
		sbuild.append(id);
		return sbuild.append("\" />").toString();
	}
	/**
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getImage() {
		return "fortification.png";
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
	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		final FortificationEvent retval = new FortificationEvent(status(), size(), getDC(), name(), getID());
		retval.setFile(getFile());
		return retval;
	}
}
