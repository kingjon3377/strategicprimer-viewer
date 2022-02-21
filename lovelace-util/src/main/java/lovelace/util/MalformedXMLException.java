package lovelace.util;

/**
 * A class of exception to throw when asked to parse (or produce) XML that is syntactically malformed.
 *
 * @deprecated Use JDK-standard classes instead in Java
 */
@Deprecated
public class MalformedXMLException extends Exception {
	private static final long serialVersionUID = 1L;
	public MalformedXMLException(final Throwable cause, final String message) {
		super(message, cause);
	}

	public MalformedXMLException(final Throwable cause) {
		this(cause, cause.getMessage());
	}

	public MalformedXMLException(final String message) {
		super(message);
	}
}
