package common.xmlio;

import javax.xml.stream.Location;

/**
 * A custom exception for XML format errors.
 *
 * TODO: Take filename as well as location in the file?
 */
public abstract class SPFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	/**
	 * The line of the XML file containing the mistake.
	 */
	private final int line;
	/**
	 * The column of the XML file where the mistake begins.
	 */
	private final int column;
	/**
	 * @param errorMessage The exception message to possibly show to the user.
	 * @param line The line of the XML file containing the mistake.
	 * @param column The column of the XML file where the mistake begins.
	 * @deprecated Use constructor taking Location if possible
	 */
	@Deprecated
	protected SPFormatException(final String errorMessage, final int line, final int column) {
		super(String.format("Incorrect SP XML at line %d, column %d: %s", line, column, errorMessage));
		this.line = line;
		this.column = column;
	}

	/**
	 * @param errorMessage The exception message to possibly show to the user.
	 * @param line The line of the XML file containing the mistake.
	 * @param column The column of the XML file where the mistake begins.
	 */
	protected SPFormatException(final String errorMessage, final Location location) {
		super(String.format("Incorrect SP XML at line %d, column %d: %s", location.getLineNumber(), location.getColumnNumber(),
				errorMessage));
		line = location.getLineNumber();
		column = location.getColumnNumber();
	}

	protected SPFormatException(final String errorMessage, final Location location, final Throwable errorCause) {
		super(String.format("Incorrect SP XML at line %d, column %d: %s", location.getLineNumber(), location.getColumnNumber(),
				errorMessage), errorCause);
		line = location.getLineNumber();
		column = location.getColumnNumber();
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
