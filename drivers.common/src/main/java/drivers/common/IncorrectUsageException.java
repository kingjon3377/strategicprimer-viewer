package drivers.common;

import java.io.Serial;

/**
 * An exception to throw when a driver fails because the user tried to use it improperly.
 */
public class IncorrectUsageException extends DriverFailedException {
	@Serial
	private static final long serialVersionUID = 1L;

	public IncorrectUsageException(final IDriverUsage correctUsage) {
		super(new IllegalArgumentException("Incorrect usage"), "Incorrect usage");
		this.correctUsage = correctUsage;
	}

	/**
	 * The "usage object" for the driver, describing its correct usage.
	 */
	private final IDriverUsage correctUsage;

	/**
	 * The "usage object" for the driver, describing its correct usage.
	 */
	public IDriverUsage getCorrectUsage() {
		return correctUsage;
	}
}
