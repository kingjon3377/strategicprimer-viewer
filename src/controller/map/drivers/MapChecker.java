package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import view.util.SystemOut;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to check every map file in a list for errors.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapChecker implements ISPDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapChecker.class
			.getName());
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();

	/**
	 * Do not instantiate.
	 */
	private MapChecker() {
		// Nothing.
	}

	/**
	 * @param args the list of filenames to check
	 */
	public static void main(final String[] args) {
		try {
			new MapChecker().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * Run the driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 1) {
//			SystemOut.SYS_OUT
//					.println("Usage: MapChecker filename [filename ...]");
			throw new DriverFailedException("Need at least one argument",
					new IllegalArgumentException("Need at least one argument"));
		}
		for (final String filename : args) {
			check(filename);
		}
	}

	/**
	 * Check a map.
	 * @param filename the name of the file to check
	 */
	private void check(final String filename) {
		SystemOut.SYS_OUT.print("Starting ");
		SystemOut.SYS_OUT.println(filename);
		boolean retval = true;
		try {
			reader.readMap(filename, Warning.INSTANCE); // new
														// Warning(Warning.Action.Warn)
		} catch (final MapVersionException e) {
			LOGGER.log(Level.SEVERE, "Map version in " + filename
					+ " not acceptable to reader", e);
			retval = false;
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, filename + " not found", e);
			retval = false;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading " + filename, e);
			retval = false;
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					"XML stream error reading " + filename, e);
			retval = false;
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE, "SP map format error reading "
					+ filename, e);
			retval = false;
		}
		if (retval) {
			SystemOut.SYS_OUT.print("No errors in ");
			SystemOut.SYS_OUT.println(filename);
		}
	}

}
