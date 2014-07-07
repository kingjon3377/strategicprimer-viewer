package controller.map.formatexceptions;

/**
 * An exception to throw when an "include" tag references a nonexistent file. We
 * need it because we can't throw FileNotFound from tag-processing functions,
 * only SPFormatExceptions.
 *
 * @author Jonathan Lovelace
 *
 */
public class MissingIncludeException extends SPFormatException {
	/**
	 * Constructor.
	 *
	 * @param file the missing file
	 * @param cause the exception that caused this one to be thrown.
	 * @param line the line the "include" tag was on.
	 */
	public MissingIncludeException(final String file, final Throwable cause,
			final int line) {
		super("File " + file + ", referenced by <include> tag on line " + line
				+ ", does not exist", line, cause);
	}
}
