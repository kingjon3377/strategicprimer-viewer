package controller.map;

/**
 * An exception to throw when an "include" tag references a file containing errors. We
 * need it because we can't throw XMLStreamException from tag-processing functions,
 * only SPFormatExceptions.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class BadIncludeException extends SPFormatException {
	/**
	 * Constructor.
	 * @param file the missing file
	 * @param cause the exception that caused this one to be thrown.
	 * @param line the line the "include" tag was on.
	 */
	public BadIncludeException(final String file, final Throwable cause, final int line) {
		super(
				"File "
						+ file
						+ ", referenced by <include> tag on specified line, contains XML format errors (see specified cause)",
				line);
		initCause(cause);
	}
}
