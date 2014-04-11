package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.stream.XMLStreamException;

import util.NullCleaner;
import util.TypesafeLogger;
import view.map.misc.SubsetFrame;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.WindowThread;

/**
 * A driver to check whether player maps are subsets of the main map and display
 * the results graphically.
 *
 *
 * TODO: Unify with SubsetDriver somehow.
 *
 * @author Jonathan Lovelace
 *
 */
public class SubsetGUIDriver implements ISPDriver {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(SubsetDriver.class);
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-s",
			"--subset", ParamCount.Many, "Check players' maps against master",
			"Check that subordinate maps are subsets of the main map, containing "
					+ "nothing that it does not contain in the same place",
			SubsetGUIDriver.class);

	/**
	 * @param args the files to check
	 */
	// ESCA-JAVA0177:
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException except) {
			LOGGER.log(Level.SEVERE,
					"Failed to switch to system look-and-feel", except);
		}
		try {
			new SubsetGUIDriver().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the main map fails to load
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 2) {
			throw new DriverFailedException("Need at least two arguments",
					new IllegalArgumentException("Need at least two arguments"));
		}
		final SubsetFrame frame = new SubsetFrame();
		SwingUtilities.invokeLater(new WindowThread(frame));
		final String first = NullCleaner.assertNotNull(args[0]);
		try {
			frame.loadMain(first);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error loading main map "
					+ first, except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map "
					+ first, except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map "
					+ first, except);
		}
		for (final String arg : args) {
			if (arg.equals(first)) {
				continue;
			}
			frame.test(arg);
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
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SubsetGUIDriver";
	}
}
