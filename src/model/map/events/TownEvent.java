package model.map.events;

import model.map.TileFixture;

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
	 * @param tStatus The status of the town
	 * @param tSize The size of the town
	 * @param discdc The DC to discover it.
	 * @param tName the name of the town, fortress, or city
	 * @param idNum the ID number.
	 */
	public TownEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum) {
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
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<town status=\"")
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
		final TownEvent retval = new TownEvent(status(), size(), getDC(),
				name(), getID());
		retval.setFile(getFile());
		return retval;
	}
}
