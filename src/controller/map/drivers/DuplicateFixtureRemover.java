package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import model.map.IMutableMapNG;
import model.map.MapNGReverseAdapter;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.resources.CacheFixture;
import util.TypesafeLogger;
import util.Warning;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce the
 * size it takes up on disk and the memory and CPU it takes to deal with it).
 *
 * TODO: Refactor the actual app out from the ISPDriver implementation.
 *
 * @author Jonathan Lovelace
 *
 */
public class DuplicateFixtureRemover implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-u",
			"--dupl", ParamCount.One, "Remove duplicate fixtures",
			"Remove duplicate fixtures---identical except ID# "
					+ "and on the same tile---from a map.",
			DuplicateFixtureRemover.class);

	/**
	 * @param args the list of maps to run the filter on
	 */
	public static void main(final String[] args) {
		try {
			new DuplicateFixtureRemover().startDriver(args);
		} catch (final DriverFailedException except) {
			TypesafeLogger.getLogger(DuplicateFixtureRemover.class).log(
					Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills,
	 * forests of the same kind, oases, etc.---we use
	 * TileFixture#equalsIgnoringID(TileFixture)) from every tile in a map.
	 *
	 * @param map the map to filter
	 * @param ostream the stream to report IDs of removed fixtures on.
	 * @throws IOException on I/O error writing to stream
	 */
	public static void filter(final IMutableMapNG map, final Appendable ostream)
			throws IOException {
		for (final Point point : map.locations()) {
			if (point != null) {
				filter(map, point, ostream);
			}
		}
	}

	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills,
	 * forests of the same kind, oases, etc.---we use
	 * TileFixture#equalsIgnoringID(TileFixture)) from a tile.
	 *
	 * @param map the map
	 * @param location the location being considered now
	 * @param ostream the stream to report IDs of removed fixtures on.
	 * @throws IOException on I/O error writing to stream
	 */
	public static void filter(final IMutableMapNG map, final Point location,
			final Appendable ostream) throws IOException {
		final List<TileFixture> fixtures = new ArrayList<>();
		final List<TileFixture> toRemove = new ArrayList<>();
		// We ignore ground and forests because they don't have IDs.
		for (final TileFixture fix : map.getOtherFixtures(location)) {
			if (fix == null) {
				continue;
			}
			boolean already = false;
			for (final TileFixture keptFixture : fixtures) {
				if (fix instanceof IUnit
						&& ((IUnit) fix).getKind().contains("TODO")
						|| fix instanceof CacheFixture) {
					break;
				} else if (keptFixture.equalsIgnoringID(fix)) {
					already = true;
					break;
				}
			}
			if (already) {
				ostream.append(fix.getClass().getName());
				ostream.append(' ');
				ostream.append(Integer.toString(fix.getID()));
				ostream.append('\n');
				toRemove.add(fix);
			} else {
				fixtures.add(fix);
			}
		}
		for (final TileFixture fix : toRemove) {
			if (fix != null) {
				map.removeFixture(location, fix);
			}
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args Command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.println("Usage: DuplicateFixtureRemover map [map ...]");
			throw new DriverFailedException("Not enough arguments",
					new IllegalArgumentException("Need at least one argument"));
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			final File file = new File(filename);
			try {
				final IMutableMapNG map =
						reader.readMap(file, Warning.INSTANCE);
				filter(map, SYS_OUT);
				reader.write(file, new MapNGReverseAdapter(map));
			} catch (final IOException except) {
				System.err.print("I/O error reading from or writing to ");
				System.err.println(filename);
				System.err.println(except.getLocalizedMessage());
				continue;
			} catch (final XMLStreamException except) {
				System.err.print("XML parsing error reading ");
				System.err.println(filename);
				System.err.println(except.getLocalizedMessage());
				continue;
			} catch (final SPFormatException except) {
				System.err.print("SP format error in ");
				System.err.println(filename);
				System.err.println(except.getLocalizedMessage());
				continue;
			}
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
		return usage().getShortDescription();
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
		return "DuplicateFixtureRemover";
	}
}
