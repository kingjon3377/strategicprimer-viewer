package util;

/**
 * A specialized runtime exception for warnings made fatal.
 * 
 * @author Jonathan Lovelace
 * 
 */
// ESCA-JAVA0051:
// ESCA-JAVA0048:
public class FatalWarning extends RuntimeException {
	/**
	 * Constructor.
	 * 
	 * @param cause the Throwable we're wrapping
	 */
	public FatalWarning(final Throwable cause) {
		super(cause);
	}
}
