package controller.map.drivers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import util.Warning;
import view.util.SystemOut;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to remove duplicate hills, forests, etc. from the map (to reduce the
 * size it takes up on disk and the memory and CPU it takes to deal with it).
 *
 * @author Jonathan Lovelace
 *
 */
public class DuplicateFixtureRemover {

	/**
	 * @param args the list of maps to run the filter on
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: DuplicateFixtureRemover map [map ...]");
		}
		final DuplicateFixtureRemover remover = new DuplicateFixtureRemover();
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (final String filename : args) {
			IMap map;
			try {
				map = reader.readMap(filename, Warning.INSTANCE);
			} catch (IOException except) {
				System.err.print("I/O error reading ");
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
			for (Point point : map.getTiles()) {
				remover.filter(map.getTile(point), SystemOut.SYS_OUT);
			}
			try {
				reader.write(filename, map);
			} catch (IOException except) {
				System.err.println("I/O error writing to a map file:");
				System.err.println(except.getLocalizedMessage());
				continue;
			}
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

}
