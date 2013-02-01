package controller.map.drivers;

/**
 * An interface for drivers, so one main() method can start different ones
 * depending on options.
 *
 * @author Jonathan Lovelace
 *
 */
public interface ISPDriver {
	/**
	 * Run the driver. If the driver is a GUIDriver, this should use
	 * SwingUtilities.invokeLater(); if it's a CLIDriver, that's not necessary.
	 * @param args any command-line arguments that should be passed to the driver.
	 * @throws DriverFailedException if it's impossible for the driver to start.
	 */
	void startDriver(final String... args) throws DriverFailedException;

	/**
	 * An exception to throw when the driver fails ... such as if the map is
	 * improperly formatted, etc. This means we don't have to declare a long
	 * list of possible exceptional circumstances.
	 */
	class DriverFailedException extends Exception {
		/**
		 * Constructor.
		 * @param cause the exception we're wrapping. Should *not* be null.
		 */
		public DriverFailedException(final Throwable cause) {
			super("The driver could not start because of an exception:", cause);
		}
	}
}
