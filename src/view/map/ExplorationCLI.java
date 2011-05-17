package view.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationRunner;
import model.viewer.SPMap;
import model.viewer.Tile;
import controller.map.MapReader;
import controller.map.MapReader.MapVersionException;

/**
 * A driver for running exploration results, etc., using the new model.
 * 
 * @author Jonathan Lovelace
 */
public final class ExplorationCLI {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ExplorationCLI.class
			.getName());
	/**
	 * The helper that actually runs the exploration.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();

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
		runner.loadAllTables("tables");
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
				} else if (input.charAt(0) == 'k') {
					runner.verboseRecursiveCheck(ostream);
				} else if (input.charAt(0) == 'h') {
					hunt(reader, map, ostream);
				}
				ostream.print("Command: ");
				input = reader.readLine();
			}
			reader.close();
		} catch (final IOException except) {
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
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_PARAM_DEREF_NONVIRTUAL")
	private void explore(final SPMap map, final BufferedReader reader)
			throws IOException {
		// ESCA-JAVA0266:
		final PrintStream ostream = System.out;
		final Tile tile = selectTile(map, reader, ostream);
		ostream.println("Tile is " + tile.getType());
		ostream.println(runner.recursiveConsultTable("main", tile));
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
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NULL_PARAM_DEREF_NONVIRTUAL")
	private void fortressInfo(final SPMap map, final BufferedReader reader)
			throws IOException {
		final PrintStream ostream = System.out;
		ostream.print(runner.defaultResults(selectTile(map, reader, ostream)));
	}
	/**
	 * @param reader the stream we read from
	 * @param ostream the stream we write to
	 * @param string the prompt
	 * @return the integer the player specified
	 * @throws IOException on I/O error
	 */
	private static int getInteger(final BufferedReader reader, final PrintStream ostream, final String string) throws IOException {
		ostream.print(string);
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException("End of input");
		}
		return Integer.parseInt(line);
	}
	/**
	 * @param map The map we're dealing with
	 * @param reader The stream we're reading from
	 * @param ostream The stream we write the prompts to
	 * @return The tile the user specifies.
	 * @throws IOException on I/O error
	 */
	private static Tile selectTile(final SPMap map, final BufferedReader reader,
			final PrintStream ostream) throws IOException {
		return map.getTile(getInteger(reader, ostream, "Row: "), getInteger(reader, ostream, "Column: "));
	}

	/**
	 * How many hours we assume a working day is for a hunter or such.
	 */
	private static final int HUNTER_HOURS = 10;
	/**
	 * How many encounters per hour for a hunter or such.
	 */
	private static final int HOURLY_ENCOUNTERS = 4;

	/**
	 * Create results for a hunter. We assume 10-hour days, and that the hunter
	 * stays on one tile all day; it's up to the Judge to run the encounters
	 * this produces.
	 * 
	 * @param reader
	 *            the stream to read coordinates from
	 * @param map
	 *            the map we're dealing with
	 * @param ostream
	 *            the stream to print them on
	 * @throws IOException
	 *             on I/O error
	 */
	private void hunt(final BufferedReader reader, final SPMap map,
			final PrintStream ostream) throws IOException {
		final Tile tile = selectTile(map, reader, ostream);
		for (int i = 0; i < HUNTER_HOURS * HOURLY_ENCOUNTERS; i++) {
			ostream.println(runner.recursiveConsultTable("hunter", tile));
		}
	}

	/**
	 * @param args
	 *            command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new ExplorationCLI(new MapReader().readMap(args[0]));
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			System.exit(1);
			return; // NOPMD;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			System.exit(2);
			return; // NOPMD;
		} catch (MapVersionException e) {
			LOGGER.log(Level.SEVERE, "Map version too old", e);
			System.exit(3);
			return; // NOPMD
		}
	}

}
