package drivers.common;

/**
 * An exception to throw when a driver fails because the user tried to use it improperly.
 */
public class IncorrectUsageException extends DriverFailedException {
	private static final long serialVersionUID = 1L;

	public IncorrectUsageException(IDriverUsage correctUsage) {
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
