package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import model.map.IMapNG;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
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
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-s",
			"--subset", ParamCount.Many, "Check players' maps against master",
			"Check that subordinate maps are subsets of the main map, containing "
					+ "nothing that it does not contain in the same place",
			SubsetDriver.class);

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
		} catch (final DriverFailedException except) {
			TypesafeLogger.getLogger(SubsetDriver.class).log(Level.SEVERE,
					except.getMessage(), except.getCause());
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
			SYS_OUT.println("Usage: SubsetDriver mainMap playerMap [playerMap ...]");
			return;
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final File mainFile = new File(args[0]);
		try {
			final IMapNG mainMap =
					reader.readMap(mainFile, new Warning(Action.Ignore));
			SYS_OUT.print("OK if strict subset, WARN if needs manual checking,");
			SYS_OUT.println("FAIL if error in reading");
			for (final String arg : args) {
				if (arg.equals(args[0])) {
					continue;
				}
				SYS_OUT.print(arg);
				SYS_OUT.print("\t...\t\t");
				printReturn(doSubsetTest(new File(arg), reader, mainMap));
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error loading main map "
					+ mainFile.getPath(), except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("XML error reading main map "
					+ mainFile.getPath(), except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("Invalid SP XML in main map "
					+ mainFile.getPath(), except);
		}
	}

	/**
	 * Print a Returns value to stdout.
	 *
	 * @param value the value to print.
	 */
	private static void printReturn(final Returns value) {
		switch (value) {
		case Fail:
			SYS_OUT.println("FAIL");
			break;
		case OK:
			SYS_OUT.println("OK");
			break;
		case Warn:
			SYS_OUT.println("WARN");
			break;
		default:
			throw new IllegalStateException("Can't get here");
		}
	}

	/**
	 * @param file a file
	 * @param reader the map reader to use
	 * @param mainMap the main map
	 * @return the result of doing a subset test on the named map
	 */
	private static Returns doSubsetTest(final File file,
			final MapReaderAdapter reader, final IMapNG mainMap) {
		try {
			final IMapNG map = reader.readMap(file, new Warning(Action.Ignore));
			if (mainMap.isSubset(map, NullCleaner.assertNotNull(System.out),
					"In " + file.getName() + ':')) {
				return Returns.OK; // NOPMD
			} else {
				System.out.flush();
				return Returns.Warn; // NOPMD
			}
		} catch (final IOException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
		} catch (final XMLStreamException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
		} catch (final SPFormatException except) {
			Warning.INSTANCE.warn(except);
			return Returns.Fail; // NOPMD
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
		return "SubsetDriver";
	}
}
