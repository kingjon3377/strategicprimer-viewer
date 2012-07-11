package controller.map.drivers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import model.map.IMap;
import model.map.PointFactory;
import model.map.Tile;
import util.SingletonRandom;
import util.Warning;
import view.util.SystemOut;
import controller.exploration.TableLoader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A class to non-interactively generate a tile's contents.
 * @author Jonathan Lovelace
 *
 */
public final class GenerateTileContents {
	/**
	 * A mapping from filenames containing maps to instances handling those maps.
	 */
	private static final Map<String, GenerateTileContents> INSTANCES = Collections
			.synchronizedMap(new HashMap<String, GenerateTileContents>());
	/**
	 * The map reader to use.
	 */
	private static final MapReaderAdapter READER = new MapReaderAdapter();
	/**
	 * @param filename the name of a map
	 * @return an instance to generate the contents of a tile on it
	 * @throws SPFormatException on SP format error in the map file
	 * @throws XMLStreamException on XML error in the map file
	 * @throws IOException on I/O error reading the file
	 * @throws MapVersionException if the reader doesn't support that map version 
	 */
	public static GenerateTileContents getInstance(final String filename)
			throws MapVersionException, IOException, XMLStreamException,
			SPFormatException {
		if (!INSTANCES.containsKey(filename)) {
			INSTANCES.put(filename, new GenerateTileContents(READER.readMap(filename, Warning.INSTANCE)));
		}
		return INSTANCES.get(filename);
	}
	/**
	 * The singleton map we'll be consulting.
	 */
	private final IMap map;
	/**
	 * Constructor.
	 * @param theMap the map we'll be consulting.
	 */
	private GenerateTileContents(final IMap theMap) {
		map = theMap;
		new TableLoader().loadAllTables("tables", runner);
	}
	/**
	 * The singleton runner we'll be using.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();
	/**
	 * Generate the contents of a tile. TODO: To match other usage, move the PointFactory usage to our caller.
	 * @param row the row of the tile
	 * @param col the column of a tile
	 * @throws MissingTableException if a missing table is referenced
	 */
	public void generateTileContents(final int row, final int col) throws MissingTableException {
		generateTileContents(map.getTile(PointFactory.point(row, col)));
	}
	/**
	 * Generate the contents of a tile.
	 * @param tile the tile
	 * @throws MissingTableException if a missing table is referenced
	 */
	private void generateTileContents(final Tile tile)
			throws MissingTableException {
		final int reps = SingletonRandom.RANDOM.nextInt(4) + 1;
		for (int i = 0; i < reps; i++) {
			println(runner.recursiveConsultTable("fisher", tile));
		}
	}
	/**
	 * @param args the map to work from, the row, and the column
	 */
	public static void main(final String[] args) {
		final Logger logger = Logger.getLogger(GenerateTileContents.class.getName());
		if (args.length < 3) {
			logger.severe("Usage: GenerateTileContents mapname.xml row col");
		} else {
			try {
				getInstance(args[0]).generateTileContents(
						Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			} catch (NumberFormatException e) {
				logger.log(Level.SEVERE, "Non-numeric row or column", e);
				System.exit(1);
			} catch (MapVersionException e) {
				logger.log(Level.SEVERE, "Unexpected map version", e);
				System.exit(2);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O error", e);
				System.exit(3);
			} catch (XMLStreamException e) {
				logger.log(Level.SEVERE, "XML error", e);
				System.exit(4);
			} catch (SPFormatException e) {
				logger.log(Level.SEVERE, "Bad SP XML format", e);
				System.exit(5);
			} catch (MissingTableException e) {
				logger.log(Level.SEVERE, "Missing table", e);
				// ESCA-JAVA0076:
				System.exit(6);
			}
		}
	}
	/**
	 * Print lines properly indented.
	 * @param text the text to print
	 */
	private static void println(final String text) {
		for (String string : text.split("\n")) {
			SystemOut.SYS_OUT.print("\t\t\t");
			SystemOut.SYS_OUT.println(string);
		}
	}
}
