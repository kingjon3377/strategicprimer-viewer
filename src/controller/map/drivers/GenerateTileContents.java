package controller.map.drivers;

import static util.NullCleaner.assertNotNull;
import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.old.ExplorationRunner;
import model.exploration.old.MissingTableException;
import model.map.IMap;
import model.map.ITile;
import model.map.Point;
import model.map.PointFactory;
import util.NullCleaner;
import util.SingletonRandom;
import util.TypesafeLogger;
import util.Warning;
import controller.exploration.TableLoader;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A class to non-interactively generate a tile's contents.
 *
 * @author Jonathan Lovelace
 *
 */
public final class GenerateTileContents {
	/**
	 * The singleton runner we'll be using.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();

	/**
	 * The singleton map we'll be consulting.
	 */
	private final IMap map;

	/**
	 * The map reader to use.
	 */
	private static final MapReaderAdapter READER = new MapReaderAdapter();

	/**
	 * A mapping from filenames containing maps to instances handling those
	 * maps.
	 */
	private static final Map<String, GenerateTileContents> INSTANCES =
			assertNotNull(Collections
					.synchronizedMap(new HashMap<String, GenerateTileContents>()));

	/**
	 * @param filename
	 *            the name of a map
	 * @return an instance to generate the contents of a tile on it
	 * @throws SPFormatException
	 *             if the reader doesn't support the specified map version or on
	 *             other SP format error in the map file
	 * @throws XMLStreamException
	 *             on XML error in the map file
	 * @throws IOException
	 *             on I/O error reading the file
	 */
	public static GenerateTileContents getInstance(final String filename)
			throws IOException, XMLStreamException, SPFormatException {
		if (!INSTANCES.containsKey(filename)) {
			INSTANCES.put(
					filename,
					new GenerateTileContents(READER.readMap(new File(filename),
							Warning.INSTANCE)));
		}
		return NullCleaner.assertNotNull(INSTANCES.get(filename));
	}

	/**
	 * Constructor.
	 *
	 * @param theMap the map we'll be consulting.
	 */
	private GenerateTileContents(final IMap theMap) {
		map = theMap;
		TableLoader.loadAllTables("tables", runner);
	}

	/**
	 * Generate the contents of a tile.
	 *
	 * @param point the tile's location
	 * @throws MissingTableException if a missing table is referenced
	 */
	public void generateTileContents(final Point point)
			throws MissingTableException {
		generateTileContents(point, map.getTile(point));
	}

	/**
	 * Generate the contents of a tile.
	 *
	 * @param tile the tile
	 * @param point the location of the tile
	 * @throws MissingTableException if a missing table is referenced
	 */
	private void generateTileContents(final Point point, final ITile tile)
			throws MissingTableException {
		final int reps = SingletonRandom.RANDOM.nextInt(4) + 1;
		for (int i = 0; i < reps; i++) {
			println(runner.recursiveConsultTable("fisher", point, tile));
		}
	}

	/**
	 * @param args the map to work from, the row, and the column
	 */
	public static void main(final String[] args) {
		final Logger logger = TypesafeLogger
				.getLogger(GenerateTileContents.class);
		if (args.length < 3) {
			logger.severe("Usage: GenerateTileContents mapname.xml row col");
		} else {
			final NumberFormat numParser = NumberFormat.getIntegerInstance();
			try {
				getInstance(NullCleaner.assertNotNull(args[0]))
						.generateTileContents(
								PointFactory.point(numParser.parse(args[1])
										.intValue(), numParser.parse(args[2])
										.intValue()));
			} catch (final NumberFormatException | ParseException e) {
				logger.log(Level.SEVERE, "Non-numeric row or column", e);
				System.exit(1);
			} catch (final MapVersionException e) {
				logger.log(Level.SEVERE, "Unexpected map version", e);
				System.exit(2);
			} catch (final IOException e) {
				logger.log(Level.SEVERE, "I/O error", e);
				System.exit(3);
			} catch (final XMLStreamException e) {
				logger.log(Level.SEVERE, "XML error", e);
				System.exit(4);
			} catch (final SPFormatException e) {
				logger.log(Level.SEVERE, "Bad SP XML format", e);
				System.exit(5);
			} catch (final MissingTableException e) {
				logger.log(Level.SEVERE, "Missing table", e);
				// ESCA-JAVA0076:
				System.exit(6);
			}
		}
	}

	/**
	 * Print lines properly indented.
	 *
	 * @param text the text to print
	 */
	private static void println(final String text) {
		for (final String string : text.split("\n")) {
			SYS_OUT.print("\t\t\t");
			SYS_OUT.println(string);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "GenerateTileContents";
	}
}
