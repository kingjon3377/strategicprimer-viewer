package util;

/**
 * A specialized runtime exception for warnings made fatal.
 * 
 * @author Jonathan Lovelace
 * 
 */
// ESCA-JAVA0051:
// ESCA-JAVA0048:
public class FatalWarningException extends RuntimeException {
	/**
	 * Constructor.
	 * 
	 * @param cause the Throwable we're wrapping
	 */
	public FatalWarningException(final Throwable cause) {
		super(cause);
	}
}
