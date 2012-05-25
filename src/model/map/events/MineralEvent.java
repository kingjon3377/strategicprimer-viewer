package model.map.events;

import model.map.HasImage;
import model.map.TileFixture;


/**
 * A vein of a mineral.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MineralEvent implements IEvent, HasImage {
	/**
	 * Constructor.
	 * 
	 * @param minkind
	 *            what kind of mineral this is
	 * @param exp
	 *            whether the vein is exposed
	 * @param discdc
	 *            the dc to discover the vein
	 * @param idNum the ID number.
	 */
	public MineralEvent(final String minkind, final boolean exp,
			final int discdc, final int idNum) {
		super();
		mineral = minkind;
		exposed = exp;
		dc = discdc;
		id = idNum;
	}

	/**
	 * What kind of mineral this is.
	 */
	private final String mineral;

	/**
	 * 
	 * @return what kind of mineral this is
	 */
	public String mineral() {
		return mineral;
	}

	/**
	 * Whether the vein is exposed. TODO: Perhaps this should be mutable and
	 * protected by accessor methods?
	 */
	private final boolean exposed;

	/**
	 * 
	 * @return whether the vein is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
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
	 * 
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder build = new StringBuilder("There is a");
		if (exposed) {
			build.append("n exposed");
		}
		build.append(" vein of ");
		build.append(mineral);
		build.append(" here");
		if (exposed) {
			build.append('.');
		} else {
			build.append(", but it's not exposed.");
		}
		return build.toString();
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof MineralEvent
						&& ((MineralEvent) obj).mineral.equals(mineral)
						&& ((MineralEvent) obj).exposed == exposed && ((TileFixture) obj).getID() == id);
	}

	/**
	 * 
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * 
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A " + mineral + " deposit, "
				+ (exposed ? "exposed" : "not exposed") + ", DC " + dc;
	}
	/**
	 * @return the kind of mineral
	 */
	public String getKind() {
		return mineral;
	}
	/**
	 * @return an XML representation of the event.
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<mineral kind=\"").append(mineral)
				.append("\" exposed=\"").append(exposed).append("\" dc=\"")
				.append(dc).append("\" id=\"").append(id).append("\" />")
				.toString();
	}
	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getImage() {
		return "mineral.png";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
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
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return this == fix
				|| (fix instanceof MineralEvent
						&& ((MineralEvent) fix).mineral.equals(mineral) && ((MineralEvent) fix).exposed == exposed);
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
		final MineralEvent retval = new MineralEvent(getKind(), isExposed(), getDC(), getID());
		retval.setFile(getFile());
		return retval;
	}
}
