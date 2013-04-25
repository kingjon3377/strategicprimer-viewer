package controller.map.drivers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import util.Warning;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.DriverUsage.ParamCount;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce the
 * size it takes up on disk and the memory and CPU it takes to deal with it).
 *
 * @author Jonathan Lovelace
 *
 */
public class DuplicateFixtureRemover implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-d",
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
		} catch (DriverFailedException except) {
			Logger.getLogger(DuplicateFixtureRemover.class.getName()).log(
					Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills,
	 * forests of the same kind, oases, etc.---we use
	 * TileFixture#equalsIgnoringID(TileFixture)) from every tile in a map.
	 * @param map the map to filter
	 * @param out the stream to report IDs of removed fixtures on.
	 */
	public void filter(final IMap map, final PrintStream out) {
		for (Point point : map.getTiles()) {
			filter(map.getTile(point), out);
		}
	}
	/**
	 * "Remove" (at first we just report) duplicate fixtures (i.e. hills,
	 * forests of the same kind, oases, etc.---we use
	 * TileFixture#equalsIgnoringID(TileFixture)) from a tile.
	 *
	 * @param tile the tile to filter
	 * @param out the stream to report IDs of removed fixtures on.
	 */
	public void filter(final Tile tile, final PrintStream out) {
		final List<TileFixture> fixtures = new ArrayList<TileFixture>();
		final List<TileFixture> toRemove = new ArrayList<TileFixture>();
		for (TileFixture fix : tile) {
			boolean already = false;
			for (TileFixture keptFixture : fixtures) {
				if ((fix instanceof Unit && ((Unit) fix).getKind().contains(
						"TODO"))
						|| fix instanceof CacheFixture) {
					break;
				} else if (keptFixture.equalsIgnoringID(fix)) {
					already = true;
					break;
				}
			}
			if (already) {
				out.print(fix.getClass().getName());
				out.print(' ');
				out.println(fix.getID());
				toRemove.add(fix);
			} else {
				fixtures.add(fix);
			}
		}
		for (TileFixture fix : toRemove) {
			tile.removeFixture(fix);
		}
	}
	/**
	 * Run the driver.
	 * @param args Command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: DuplicateFixtureRemover map [map ...]");
			throw new DriverFailedException("Not enough arguments", new IllegalArgumentException("Need at least one argument"));
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (final String filename : args) {
			try {
				final IMap map = reader.readMap(filename, Warning.INSTANCE);
				filter(map, SystemOut.SYS_OUT);
				reader.write(filename, map);
			} catch (IOException except) {
				System.err.print("I/O error reading from or writing to ");
				System.err.println(filename);
				System.err.println(except.getLocalizedMessage());
				continue;
			} catch (XMLStreamException except) {
				System.err.print("XML parsing error reading ");
				System.err.println(filename);
				System.err.println(except.getLocalizedMessage());
				continue;
			} catch (SPFormatException except) {
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
