package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import util.Warning;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to check whether player maps are subsets of the main map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class SubsetDriver implements ISPDriver {
	/**
	 * Possible return values for sub-maps.
	 */
	private enum Returns {
		/**
		 * The map is a subset.
		 */
		OK,
		/**
		 * The map isn't a subset.
		 */
		Warn,
		/**
		 * The map failed to load.
		 */
		Fail;
	}
	/**
	 * @param args the files to check
	 */
	// ESCA-JAVA0177:
	public static void main(final String[] args) {
		try {
			new SubsetDriver().startDriver(args);
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
			SystemOut.SYS_OUT
					.println("Usage: SubsetDriver mainMap playerMap [playerMap ...]");
			return;
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		IMap mainMap;
		try {
			mainMap = reader.readMap(args[0], new Warning(// NOPMD
					Warning.Action.Ignore));
		} catch (IOException except) {
			throw new DriverFailedException("I/O error loading main map " + args[0], except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map " + args[0], except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map " + args[0], except);
		}
		SystemOut.SYS_OUT
				.print("OK if strict subset, WARN if needs manual checking,");
		SystemOut.SYS_OUT.println("FAIL if error in reading");
		for (final String arg : args) {
			if (arg.equals(args[0])) {
				continue;
			}
			SystemOut.SYS_OUT.print(arg);
			SystemOut.SYS_OUT.print("\t...\t\t");
			printReturn(doSubsetTest(arg, reader, mainMap));
		}
	}
	/**
	 * Print a Returns value to stdout.
	 * @param value the value to print.
	 */
	private static void printReturn(final Returns value) {
		switch (value) {
		case Fail:
			SystemOut.SYS_OUT.println("FAIL");
			break;
		case OK:
			SystemOut.SYS_OUT.println("OK");
			break;
		case Warn:
			SystemOut.SYS_OUT.println("WARN");
			break;
		default:
			throw new IllegalStateException("Can't get here");
		}
	}
	/**
	 * @param filename a filename
	 * @param reader the map reader to use
	 * @param mainMap the main map
	 * @return the result of doing a subset test on the named map
	 */
	private static Returns doSubsetTest(final String filename,
			final MapReaderAdapter reader, final IMap mainMap) {
		final IMap map; // NOPMD
		try {
			map = reader.readMap(filename, new Warning(Warning.Action.Ignore));
		} catch (IOException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
		} catch (XMLStreamException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
		} catch (SPFormatException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
		}
		return mainMap.isSubset(map, SystemOut.SYS_OUT) ? Returns.OK : Returns.Warn;
	}
}
