package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.events.TownStatus;

/**
 * A mine---a source of mineral resources.
 * @author Jonathan Lovelace
 *
 */
public class Mine implements TileFixture, HasImage {
	/**
	 * Constructor.
	 * @param mineral what mineral this produces
	 * @param stat the status of the mine
	 */
	public Mine(final String mineral, final TownStatus stat) {
		kind = mineral;
		status = stat;
	}
	/**
	 * What the mine produces.
	 */
	private final String kind;
	/**
	 * The status of the mine.
	 */
	private final TownStatus status;
	/**
	 * @return what the mine produces
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * @return the status of the mine
	 */
	public TownStatus getStatus() {
		return status;
	}
	/**
	 * @return an XML representation of the mine
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<mine kind=\"").append(kind)
				.append("\" status=\"").append(status.toString())
				.append("\" />").toString();
	}
	/**
	 * @return the name of an image to represent the mine
	 */
	@Override
	public String getImage() {
		return "mine.png";
	}
	/**
	 * @return a string representation of the mine
	 */
	@Override
	public String toString() {
		return getStatus().toString() + " mine of " + getKind();
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
		return obj instanceof Mine && kind.equals(((Mine) obj).kind)
				&& status.equals(((Mine) obj).status);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode() << status.ordinal();
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
