package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.events.TownStatus;

/**
 * A village on the map.
 * @author Jonathan Lovelace
 *
 */
public class Village implements TileFixture, HasImage {
	/**
	 * The status of the village.
	 */
	private final TownStatus status;
	/**
	 * The name of the village.
	 */
	private final String name;
	/**
	 * Constructor.
	 * @param vstatus the status of the village.
	 * @param vName the name of the village
	 */
	public Village(final TownStatus vstatus, final String vName) {
		status = vstatus;
		name = vName;
	}
	/**
	 * @return the status of the village
	 */
	public TownStatus getStatus() {
		return status;
	}
	/**
	 * @return an XML representation of the village
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<village status=\"");
		sbuild.append(status.toString());
		if (!"".equals(name)) {
			sbuild.append("\" name=\"");
			sbuild.append(name);
		}
		sbuild.append("\" />");
		return sbuild.toString();
	}
	/**
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		return status.toString() + " village" + ("".equals(name) ? name : " named " + name);
	}
	/**
	 * @return the name of an image to represent the village
	 */
	@Override
	public String getImage() {
		return "village.png";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 45;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Village && status.equals(((Village) obj).status)
				&& name.equals(((Village) obj).name);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return status.ordinal() | name.hashCode();
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
}
