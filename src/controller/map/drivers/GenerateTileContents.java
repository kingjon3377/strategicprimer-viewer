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
public class GenerateTileContents {
	/**
	 * The singleton map we'll be consulting.
	 */
	private static SPMap map = null;
	/**
	 * The singleton runner we'll be using.
	 */
	private static final ExplorationRunner RUNNER = new ExplorationRunner();
	static {
		new TableLoader().loadAllTables("tables", RUNNER);
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
				if (map == null) {
					map = new MapReaderAdapter().readMap(args[0]);
				}
				Tile tile = map.getTile(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			final int reps = SingletonRandom.RANDOM.nextInt(6) + 1;
			for (int i = 0; i < reps; i++) {
				println(RUNNER.recursiveConsultTable("main", tile));
			}
			} catch (NumberFormatException e) {
				logger.log(Level.SEVERE, "Non-numeric row or column", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
				System.exit(1);
			} catch (MapVersionException e) {
				logger.log(Level.SEVERE, "Unexpected map version", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
				System.exit(2);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O error", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
				System.exit(3);
			} catch (XMLStreamException e) {
				logger.log(Level.SEVERE, "XML error", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
				System.exit(4);
			} catch (SPFormatException e) {
				logger.log(Level.SEVERE, "Bad SP XML format", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
				System.exit(5);
			} catch (MissingTableException e) {
				logger.log(Level.SEVERE, "Missing table", e);
				e.printStackTrace();
				e.printStackTrace(SystemOut.SYS_OUT);
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
