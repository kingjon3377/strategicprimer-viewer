package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
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
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-e",
			"--echo", ParamCount.One, "Read, then write a map.",
			"Read and write a map, correcting deprecated syntax.",
			EchoDriver.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(EchoDriver.class);

	/**
	 * Main.
	 *
	 * @param args command-line arguments: the filename to read from and the
	 *        filename to write to. These may be the same.
	 */
	public static void main(final String[] args) {
		try {
			new EchoDriver().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Run the driver.
	 *
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
		final String infile = args[0];
		final String outfile = args[1];
		assert infile != null;
		assert outfile != null;
		try {
			map = new MapReaderAdapter().readMap(infile, new Warning(// NOPMD
					Action.Ignore));
		} catch (final MapVersionException except) {
			throw new DriverFailedException("Unsupported map version", except);
		} catch (final IOException except) {
			throw new DriverFailedException(
					"I/O error reading file " + infile, except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error", except);
		}
		try {
			new MapReaderAdapter().write(outfile, map);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error writing " + outfile,
					except);
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
		return "EchoDriver";
	}
}
