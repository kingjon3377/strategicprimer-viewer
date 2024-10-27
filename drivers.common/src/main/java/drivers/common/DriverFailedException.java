package drivers.common;

import java.io.Serial;

/**
 * An exception to throw whenever a driver fails, so drivers only have to directly handle one exception class.
 */
public class DriverFailedException extends Exception {
	@Serial
	private static final long serialVersionUID = 1L;

	public DriverFailedException(final Throwable cause, final String message) {
		super(message, cause);
	}

	public DriverFailedException(final Throwable cause) {
		this(cause, "The app could not start because of an exception:");
	}

	@SuppressWarnings("UseOfConcreteClass")
	public static DriverFailedException illegalState(final String message) {
		return new DriverFailedException(new IllegalStateException(message), message);
	}
}
