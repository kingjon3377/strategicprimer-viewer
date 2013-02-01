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
	 */
	void startDriver(final String... args);
}
