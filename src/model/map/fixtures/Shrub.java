package model.map.fixtures;

import model.map.TileFixture;

/**
 * A TileFixture to represent shrubs, or their aquatic equivalents, on a tile.
 * @author Jonathan Lovelace
 *
 */
public class Shrub implements TileFixture {
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
}
