package view.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import controller.map.XMLReader;

import model.exploration.ExplorationRunner;
import model.viewer.SPMap;
import model.viewer.Tile;

/**
 * A driver for running exploration results, etc., using the new model.
 * 
 * @author Jonathan Lovelace
 */
public final class ExplorationCLI {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(ExplorationCLI.class
			.getName());

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE")
	private ExplorationCLI(final SPMap map) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		// ESCA-JAVA0266:
		final PrintStream ostream = System.out;
		if (ostream == null) {
			throw new IllegalStateException("System.out is null");
		}
		try {
			ostream.print("Command: ");
			String input = reader.readLine();
			while (input != null && input.length() > 0) {
				if (input.charAt(0) == 'q') {
					break;
				} else if (input.charAt(0) == 'x') {
					explore(map, reader);
				} else if (input.charAt(0) == 'f') {
					fortressInfo(map, reader);
				}
				ostream.print("Command: ");
				input = reader.readLine();
			}
			reader.close();
		} catch (IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * Explore a user-specified tile.
	 * 
	 * @param map
	 *            the map
	 * @param reader
	 *            source of user input
	 * @throws IOException
	 *             on I/O error
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL")
	private void explore(final SPMap map, final BufferedReader reader)
			throws IOException {
		// ESCA-JAVA0266:
		final PrintStream ostream = System.out;
		ostream.print("Row: ");
		final int row = Integer.parseInt(reader.readLine());
		ostream.print("Column: ");
		final int col = Integer.parseInt(reader.readLine());
		final Tile tile = map.getTile(row, col);
		// TODO: add algorithm from tables
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 * 
	 * @param map
	 *            the map
	 * @param reader
	 *            source of user input
	 * @throws IOException
	 *             on I/O error
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_ALWAYS_NULL")
	private static void fortressInfo(final SPMap map,
			final BufferedReader reader) throws IOException {
		final PrintStream ostream = System.out;
		ostream.print("Row: ");
		final int row = Integer.parseInt(reader.readLine());
		ostream.print("Column: ");
		final int col = Integer.parseInt(reader.readLine());
		final Tile tile = map.getTile(row, col);
		ostream.print(new ExplorationRunner().defaultResults(tile));
	}

	/**
	 * @param args
	 *            command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new ExplorationCLI(new XMLReader().getMap(args[0]));
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			System.exit(1);
			return; // NOPMD;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			System.exit(2);
			return; // NOPMD;
		}
	}

}
