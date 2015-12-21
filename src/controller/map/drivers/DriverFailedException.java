package controller.map.drivers;

/**
 * An exception to throw when the driver fails ... such as if the map is
 * improperly formatted, etc. This means we don't have to declare a long
 * list of possible exceptional circumstances.
 * @author Jonathan Lovelace
 */
public class DriverFailedException extends Exception { // $codepro.audit.disable
	/**
	 * Constructor.
	 *
	 * @param cause the exception we're wrapping. Should *not* be null.
	 */
	public DriverFailedException(final Throwable cause) {
		super("The driver could not start because of an exception:", cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message a custom error string
	 * @param cause the cause. Should not be null.
	 */
	public DriverFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
