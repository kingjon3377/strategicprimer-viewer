package legacy.map.fixtures.resources;

/**
 * Whether a field, grove, etc., is wild or cultivated
 *
 * @author Jonathan Lovelace
 */
public enum CultivationStatus {
	/**
	 * Wild or abandoned.
	 */
	WILD,
	/**
	 * Under active cultivation by a town of some kind.
	 */
	CULTIVATED;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public String capitalized() {
		return name().charAt(0) + name().substring(1);
	}
}
