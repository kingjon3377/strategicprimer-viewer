package drivers.common;

/**
 * An interface for drivers which operate on a map model of some kind.
 */
public interface ModelDriver /*of CLIDriver|ReadOnlyDriver|GUIDriver*/ extends ISPDriver {
	/**
	 * Run the driver on a driver model.
	 */
	void startDriver() throws DriverFailedException;

	/**
	 * The underlying model.
	 */
	IDriverModel getModel();
}
