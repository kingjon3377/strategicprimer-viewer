package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A TileFixture to represent shrubs, or their aquatic equivalents, on a tile.
 * @author Jonathan Lovelace
 *
 */
public class Shrub implements TileFixture, HasImage {
	/**
	 * @return an XML representation of the Fixture.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<shrub shrub=\"").append(description)
				.append("\" />").toString();
	}
	/**
	 * A description of what kind of shrub this is.
	 */
	private final String description;
	/**
	 * Constructor.
	 * @param desc a description of the shrub.
	 */
	public Shrub(final String desc) {
		description = desc;
	}
	/**
	 * @return a description of the shrub
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the name of an image to represent the shrub.
	 */
	@Override
	public String getImage() {
		return "shrub.png";
	}
	/**
	 * @return the description of the shrub
	 */
	@Override
	public String toString() {
		return getDescription();
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 15;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Shrub && description.equals(((Shrub) obj).description);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return description.hashCode();
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.getZValue() - getZValue();
	}
}
