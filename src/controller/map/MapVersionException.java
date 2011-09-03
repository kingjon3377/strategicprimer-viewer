package controller.map;

/**
 * An exception to throw when the map's version is too old.
 * @author Jonathan Lovelace
 */
public final class MapVersionException extends Exception {
	/**
	 * Constructor.
	 * @param message the message to show the user if this isn't caught.
	 */
	public MapVersionException(final String message) {
		super(message);
	}
}
