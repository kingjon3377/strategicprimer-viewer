package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import view.map.misc.SubsetFrame;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;

/**
 * A driver to check whether player maps are subsets of the main map and display
 * the results graphically.
 *
 * @author Jonathan Lovelace
 *
 */
public class SubsetGUIDriver implements ISPDriver {
	/**
	 * @param args the files to check
	 */
	// ESCA-JAVA0177:
	public static void main(final String[] args) {
		try {
			new SubsetGUIDriver().startDriver(args);
		} catch (DriverFailedException except) {
			Logger.getLogger(SubsetDriver.class.getName()).log(
					Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * Run the driver.
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
		try {
			frame.loadMain(args[0]);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error loading main map " + args[0], except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map " + args[0], except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map " + args[0], except);
		}
		for (final String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			frame.test(arg);
		}
	}
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-s",
			"--subset", ParamCount.Many, "Check players' maps against master",
			"Check that subordinate maps are subsets of the main map, containing "
					+ "nothing that it does not contain in the same place",
			SubsetGUIDriver.class);

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
}
