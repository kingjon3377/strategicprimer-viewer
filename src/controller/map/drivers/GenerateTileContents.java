package controller.map.drivers;

import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import model.map.Tile;
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
	 * @param args the map to work from, the row, and the column
	 */
	public static void main(final String[] args) {
		final Logger logger = Logger.getLogger(GenerateTileContents.class.getName());
		if (args.length < 3) {
			logger.severe("Usage: GenerateTileContents mapname.xml row col");
		} else {
			Tile tile;
			try {
				tile = new MapReaderAdapter().readMap(args[0]).getTile(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			final ExplorationRunner runner = new ExplorationRunner();
			new TableLoader().loadAllTables("tables", runner);
			final int reps = new Random(System.currentTimeMillis()).nextInt(6) + 1;
			for (int i = 0; i < reps; i++) {
				println(runner.recursiveConsultTable("main", tile));
			}
			} catch (NumberFormatException e) {
				logger.log(Level.SEVERE, "Non-numeric row or column", e);
			} catch (MapVersionException e) {
				logger.log(Level.SEVERE, "Unexpected map version", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "I/O error", e);
			} catch (XMLStreamException e) {
				logger.log(Level.SEVERE, "XML error", e);
			} catch (SPFormatException e) {
				logger.log(Level.SEVERE, "Bad SP XML format", e);
			} catch (MissingTableException e) {
				logger.log(Level.SEVERE, "Missing table", e);
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
