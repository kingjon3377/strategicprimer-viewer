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
public final class EchoDriver {
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
		if (args.length != 2) {
			System.err.println("Usage: EchoDriver in-file out-file");
			System.exit(1);
		}
		// ESCA-JAVA0177:
		final IMap map; // NOPMD
		try {
			map = new MapReaderAdapter().readMap(args[0], new Warning(// NOPMD
					Warning.Action.Ignore));
		} catch (final MapVersionException except) {
			LOGGER.log(Level.SEVERE, "Unsupported map version", except);
			System.exit(2);
			return; // NOPMD
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading file " + args[0],
					except);
			System.exit(3);
			return; // NOPMD
		} catch (final XMLStreamException except) {
			LOGGER.log(Level.SEVERE, "Malformed XML", except);
			System.exit(4);
			return; // NOPMD
		} catch (final SPFormatException except) {
			LOGGER.log(Level.SEVERE, "SP map format error", except);
			System.exit(5);
			return; // NOPMD
		}
		try {
			new MapReaderAdapter().write(args[1], map);
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error writing " + args[1], except);
		}

	}
}
