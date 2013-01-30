package controller.map.formatexceptions;

/**
 * An exception to throw when the map's version is too old.
 * 
 * @author Jonathan Lovelace
 */
public final class MapVersionException extends SPFormatException {
	/**
	 * Constructor.
	 * 
	 * @param message the message to show the user if this isn't caught.
	 */
	public MapVersionException(final String message) {
		super(message, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param message the message to show the user if this isn't caught.
	 * @param line the line of the map tag.
	 */
	public MapVersionException(final String message, final int line) {
		super(message, line);
	}
}
