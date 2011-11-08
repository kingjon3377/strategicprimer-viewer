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
	 * Constructor.
	 * @param vstatus the status of the village.
	 */
	public Village(final TownStatus vstatus) {
		status = vstatus;
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
		return new StringBuilder("<village status=\"")
				.append(status.toString()).append("\" />").toString();
	}
	/**
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		return status.toString() + " village";
	}
	/**
	 * @return the name of an image to represent the village
	 */
	@Override
	public String getImage() {
		return "village.png";
	}
}
