package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import util.Warning;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver that reads in maps and then writes them out again---this is
 * primarily to make sure that the map format is properly read, but is also
 * useful for correcting deprecated syntax. (Because of that usage, warnings are
 * disabled.)
 *
 * @author Jonathan Lovelace
 *
 */
public final class EchoDriver implements ISPDriver {
	/**
	 * Do not instantiate.
	 */
	private EchoDriver() {
		// Do nothing.
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(EchoDriver.class
			.getName());

	/**
	 * Main.
	 *
	 * @param args command-line arguments: the filename to read from and the
	 *        filename to write to. These may be the same.
	 */
	public static void main(final String[] args) {
		try {
			new EchoDriver().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * Run the driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length != 2) {
			System.err.println("Usage: EchoDriver in-file out-file");
			throw new DriverFailedException("Wrong number of arguments",
					new IllegalArgumentException("Need exactly two arguments"));
		}
		// ESCA-JAVA0177:
		final IMap map; // NOPMD
		try {
			map = new MapReaderAdapter().readMap(args[0], new Warning(// NOPMD
					Warning.Action.Ignore));
		} catch (final MapVersionException except) {
			throw new DriverFailedException("Unsupported map version", except);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error reading file " + args[0],
					except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error", except);
		}
		try {
			new MapReaderAdapter().write(args[1], map);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error writing " + args[1], except);
		}
	}
}
