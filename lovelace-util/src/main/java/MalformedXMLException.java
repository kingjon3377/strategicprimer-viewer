/**
 * A class of exception to throw when asked to parse (or produce) XML that is syntactically malformed.
 *
 * @deprecated Use JDK-standard classes instead in Java
 */
@Deprecated
public class MalformedXMLException extends Exception {
	public MalformedXMLException(Throwable cause, String message) {
		super(message, cause);
	}

	public MalformedXMLException(Throwable cause) {
		this(cause, cause.getMessage());
	}

	public MalformedXMLException(String message) {
		super(message);
	}
}
