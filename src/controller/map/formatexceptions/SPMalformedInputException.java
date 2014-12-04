package controller.map.formatexceptions;
/**
 * For cases of malformed input where we can't use XMLStreamException.
 * @author Jonathan Lovelace
 *
 */
public class SPMalformedInputException extends SPFormatException {
	/**
	 * Constructor.
	 * @param line where this occurred
	 */
	public SPMalformedInputException(final int line) {
		super("Malformed input", line);
	}
	/**
	 * @param line where this occurred
	 * @param cause the underlying exception
	 */
	public SPMalformedInputException(final int line, final Throwable cause) {
		super("Malformed input", line, cause);
	}
}
