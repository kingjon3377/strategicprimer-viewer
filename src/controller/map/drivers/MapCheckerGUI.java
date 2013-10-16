package controller.map.drivers;

import java.util.logging.Level;
import java.util.logging.Logger;

import util.TypesafeLogger;
import view.map.misc.MapCheckerFrame;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;

/**
 * A driver to check every map file in a list for errors and report the results
 * in a window.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapCheckerGUI implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-k",
			"--check", ParamCount.Many, "Check map for errors",
			"Check a map file for errors, deprecated syntax, etc.",
			MapCheckerGUI.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(MapCheckerGUI.class);

	/**
	 * @param args the list of filenames to check
	 */
	public static void main(final String[] args) {
		try {
			new MapCheckerGUI().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final MapCheckerFrame window = new MapCheckerFrame();
		window.setVisible(true);
		for (final String filename : args) {
			window.check(filename);
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
}
