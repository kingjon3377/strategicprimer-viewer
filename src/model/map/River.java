package model.map;


/**
 * If a tile has a river, it could be in any one of several directions. This
 * class enumerates those. Tiles should have a <em>set</em> of these.
 * 
 * At present we'll just cover the four cardinal directions.
 * 
 * @author Jonathan Lovelace
 * 
 */
public enum River implements XMLWritable {
	/**
	 * North.
	 */
	North("north"),
	/**
	 * East.
	 */
	East("east"),
	/**
	 * South.
	 */
	South("south"),
	/**
	 * West.
	 */
	West("west"),
	/**
	 * A lake (to be depicted as being in the center of the tile).
	 */
	Lake("lake");
	/**
	 * A descriptive string representing the direction.
	 */
	private final String desc;
	/**
	 * Constructor.
	 * @param string a descriptive string representing the direction
	 */
	private River(final String string) {
		desc = string;
	}
	/**
	 * Parse a river direction.
	 * 
	 * @param string
	 *            a string giving the direction
	 * 
	 * @return the river direction
	 */
	public static River getRiver(final String string) {
		for (River river : values()) {
			if (river.desc.equals(string)) {
				return river;
			}
		}
			throw new IllegalArgumentException(
					"Unrecognized river direction string");
	}
	/**
	 * @return an XML representation of the river.
	 */
	@Override
	public String toXML() {
		if (Lake.equals(this)) {
			return "<lake />"; // NOPMD
		} else {
			return new StringBuilder("<river direction=\"")
					.append(desc).append("\" />").toString();
		}
	}
}
