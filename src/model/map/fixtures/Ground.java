package model.map.fixtures;

import model.map.TileFixture;
/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 * @author Jonathan Lovelace
 *
 */
public class Ground implements TileFixture {
	/**
	 * @return an XML representation of the Fixture.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<ground ground=\"").append(description)
				.append("\" exposed=\"").append(exposed).append("\" />")
				.toString();
	}
	/**
	 * Constructor.
	 * @param desc a description of the ground (the type of rock)
	 * @param exp whether it's exposed. (If not, the tile should also include a grass or forest Fixture ...)  
	 */
	public Ground(final String desc, final boolean exp) {
		description = desc;
		exposed = exp;
	}
	/**
	 * A description of the ground (the kind of rock).
	 */
	private final String description;
	/**
	 * Whether the ground is exposed.
	 */
	private final boolean exposed;
	/**
	 * @return whether the ground is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}
	/**
	 * @return a description of the grond
	 */
	public String getDescription() {
		return description;
	}
}
