package common.xmlio;

/**
 * A custom exception for XML format errors.
 */
public abstract class SPFormatException extends Exception {
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
	 */
	// TODO: Take Location instead of row and column number, now we don't have to worry about possible JS interop anymore
	public SPFormatException(String errorMessage, int line, int column) {
		super(String.format("Incorrect SP XML at line %d, column %d: %s", line, column, errorMessage));
		this.line = line;
		this.column = column;
	}

	public SPFormatException(String errorMessage, int line, int column, Throwable errorCause) {
		super(String.format("Incorrect SP XML at line %d, column %d: %s", line, column, errorMessage), errorCause);
		this.line = line;
		this.column = column;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
