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
	 *
	 * @param string a descriptive string representing the direction
	 */
	private River(final String string) {
		desc = string;
	}

	/**
	 * Parse a river direction.
	 *
	 * @param string a string giving the direction
	 *
	 * @return the river direction
	 */
	public static River getRiver(final String string) {
		for (final River river : values()) {
			if (river.desc.equals(string)) {
				return river;
			}
		}
		throw new IllegalArgumentException(
				"Unrecognized river direction string");
	}

	/**
	 * @return a description of the direction of the river
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * @return an XML representation of the river.
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		if (this == Lake) {
			return "<lake />"; // NOPMD
		} else {
			return new StringBuilder("<river direction=\"").append(desc)
					.append("\" />").toString();
		}
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
}
