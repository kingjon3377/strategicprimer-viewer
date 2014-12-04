package controller.map.formatexceptions;

/**
 * A custom exception for XML format errors.
 *
 * TODO: Take Location rather than int for the location in the XML.
 *
 * @author Jonathan Lovelace
 *
 */
public class SPFormatException extends Exception {
	/**
	 * The line of the XML file containing the mistake.
	 */
	private final int line;

	/**
	 *
	 * @return the line of the XML file containing the mistake
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Constructor.
	 *
	 * @param message a message describing what's wrong with the XML.
	 * @param errorLine the line containing the error.
	 */
	protected SPFormatException(final String message, final int errorLine) {
		super("Incorrect SP XML at line " + errorLine + ": " + message);
		line = errorLine;
	}
	/**
	 * @param message a message describing what's wrong with the XML
	 * @param errorLine the line containing the error
	 * @param cause the "initial cause" of this
	 */
	protected SPFormatException(final String message, final int errorLine,
			final Throwable cause) {
		super("Incorrect SP XML at line " + errorLine + ": " + message, cause);
		line = errorLine;
	}
}
