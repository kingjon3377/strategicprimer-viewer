package common.map;

/**
 * Something that can go on a tile."
 *
 * TODO: Any other members?
 */
public interface TileFixture extends IFixture, Comparable<TileFixture> {
	/**
	 * A <em>short</em>, no more than one line and preferably no more than two
	 * dozen characters, description of the fixture, suitable for saying what
	 * it is when an explorer happens on it.
	 */
	String getShortDescription();

	/**
	 * Clone the object.
	 */
	@Override
	TileFixture copy(boolean zero);

	/**
	 * Compare to another fixture.
	 */
	@Override
	default int compareTo(TileFixture other) {
		return Integer.compare(hashCode(), other.hashCode());
	}

	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * Some rough guidelines for the scale:
	 *
	 * <ul>
	 * <li>0 is "impossible to miss": the type of terrain you pass through</li>
	 * <li>10 and under is "hard to miss": forests, mountains, rivers, perhaps hills</li>
	 * <li>10-20 is "not hard to spot": shrubs, active populations</li>
	 * <li>20-30 is "you have to be observant": ruins, etc.</li>
	 * <li>30+ is generally "<em>really</em> observant or specialized
	 * equipment": unexposed mineral deposits, portals to other worlds, etc.</li>
	 * </ul>
	 *
	 * TODO: In many or most cases, DCs should take surrounding-terrain
	 * context into account, and so this shouldn't be an instance function
	 * here, but either on the map, in a separate class, or in a toplevel function.
	 */
	int getDC();

	/**
	 * Whether this fixture should be skipped in strict-subset calculations (at the map level).
	 */
	default boolean subsetShouldSkip() {
		return false;
	}
}
