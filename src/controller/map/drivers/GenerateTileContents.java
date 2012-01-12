package controller.map.drivers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import model.map.SPMap;
import model.map.Tile;
import util.SingletonRandom;
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
	 * Singleton object.
	 */
	private static GenerateTileContents singleton = null;
	/**
	 * Object to synchronize on.
	 */
	private static final Object LOCK = "";
	/**
	 * @param map the map to refer to if we have to create a new object
	 * @return the singleton
	 */
	public static GenerateTileContents getSingleton(final SPMap map) {
		synchronized (LOCK) {
			if (singleton == null) {
				singleton = new GenerateTileContents(map);
			}
		}
		return singleton;
	}
	/**
	 * A kind of exception to throw if we need an argument.
	 */
	public static final class NotYetInitializedException extends Exception {
		/**
		 * Constructor.
		 * @param message the message to give
		 */
		public NotYetInitializedException(final String message) {
			super(message);
		}
	}
	/**
	 * @return the singleton, if it's already been initialized
	 * @throws NotYetInitializedException if it hasn't already been initialized
	 */
	public static GenerateTileContents getSingleton() throws NotYetInitializedException {
		synchronized (LOCK) {
			if (singleton == null) {
				throw new NotYetInitializedException("Singleton hasn't been initialized yet; pass in a map");
			}
		}
		return singleton;
	}
	/**
	 * The singleton map we'll be consulting.
	 */
	private final SPMap map;
	/**
	 * Constructor.
	 * @param theMap the map we'll be consulting.
	 */
	private GenerateTileContents(final SPMap theMap) {
		map = theMap;
		new TableLoader().loadAllTables("tables", runner);
	}
	/**
	 * The singleton runner we'll be using.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();
	/**
	 * Generate the contents of a tile.
	 * @param row the row of the tile
	 * @param col the column of a tile
	 * @throws MissingTableException if a missing table is referenced
	 */
	public void generateTileContents(final int row, final int col) throws MissingTableException {
		final Tile tile = map.getTile(row, col);
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
				// ESCA-JAVA0177:
				GenerateTileContents generator; // NOPMD
				try {
					generator = getSingleton();
				} catch (NotYetInitializedException e) {
					generator = getSingleton(new MapReaderAdapter().readMap(args[0]));
				}
				generator.generateTileContents(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
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
