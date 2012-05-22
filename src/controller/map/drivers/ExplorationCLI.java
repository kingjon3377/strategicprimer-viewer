package controller.map.drivers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import model.map.IMap;
import model.map.Tile;
import util.Warning;
import view.util.DriverQuit;
import view.util.SystemOut;
import controller.exploration.TableLoader;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

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
	 * Random number generator.
	 */
	private final Random random = new Random(System.currentTimeMillis());
	/**
	 * Constructor.
	 */
	private ExplorationCLI() {
		new TableLoader().loadAllTables("tables", runner);
	}

	/**
	 * @param map the map to explore
	 * @param reader the stream to read commands from
	 * @param ostream the stream to write output to
	 */
	private void repl(final IMap map, final BufferedReader reader,
			final PrintStream ostream) {
		try {
			ostream.print("Command: ");
			String input = reader.readLine();
			while (canKeepGoing(input)) {
				handleCommand(map, reader, ostream, input.charAt(0));
				ostream.print("Command: ");
				input = reader.readLine();
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		} finally {
			try {
				reader.close();
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE, "I/O exception while closing reader",
						except);
			}
		}
	}

	/**
	 * @param map
	 *            the map
	 * @param reader
	 *            the stream to read further input from
	 * @param ostream
	 *            the stream to write to
	 * @param input
	 *            the command
	 * 
	 * @throws IOException
	 *             on I/O error
	 */
	public void handleCommand(final IMap map, final BufferedReader reader,
			final PrintStream ostream, final char input) throws IOException {
		switch (input) {
		case 'x':
			explore(map, reader, ostream);
			break;
		case 'f':
			fortressInfo(map, reader, ostream);
			break;
		case 'k':
			runner.verboseRecursiveCheck(ostream);
			break;
		case 'h':
		case 'g':
		case 'i':
			repeatedlyConsultTable(ORDER_MAP.get(Character.valueOf(input)),
					selectTile(map, reader, ostream), HUNTER_HOURS
							* HOURLY_ENCOUNTERS, ostream);
			break;
		default:
			ostream.println("Unknown command.");
			break;
		}
	}

	/**
	 * We keep looping if the input is not null, is not an empty string, and is
	 * not "quit" (only the first character is checked).
	 * 
	 * @param input
	 *            a line of input
	 * 
	 * @return whether that input says we should keep going.
	 */
	private static boolean canKeepGoing(final String input) {
		return input != null && input.length() > 0 && input.charAt(0) != 'q';
	}

	/**
	 * Explore a user-specified tile.
	 * 
	 * @param map
	 *            the map
	 * @param reader
	 *            source of user input
	 * @param ostream
	 *            the stream to write the results to
	 * 
	 * @throws IOException
	 *             on I/O error
	 */
	private void explore(final IMap map, final BufferedReader reader,
			final PrintStream ostream) throws IOException {
		final Tile tile = selectTile(map, reader, ostream);
		ostream.print("Tile is ");
		ostream.println(tile.getTerrain());
		try {
			final int reps = random.nextInt(6) + 1;
			for (int i = 0; i < reps; i++) {
				ostream.println(runner.recursiveConsultTable("main", tile));
			}
		} catch (MissingTableException e) {
			LOGGER.log(Level.SEVERE, "Missing table", e);
		}
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 * 
	 * @param map
	 *            the map
	 * @param reader
	 *            source of user input
	 * @param ostream
	 *            the stream to print results to
	 * 
	 * @throws IOException
	 *             on I/O error
	 */
	private void fortressInfo(final IMap map, final BufferedReader reader,
			final PrintStream ostream) throws IOException {
		try {
			ostream.print(runner.defaultResults(selectTile(map, reader, ostream)));
		} catch (MissingTableException e) {
			LOGGER.log(Level.SEVERE, "Missing table", e);
		}
	}

	/**
	 * @param reader
	 *            the stream we read from
	 * @param ostream
	 *            the stream we write to
	 * @param string
	 *            the prompt
	 * @return the integer the player specified
	 * @throws IOException
	 *             on I/O error
	 */
	private static int getInteger(final BufferedReader reader,
			final PrintStream ostream, final String string) throws IOException {
		ostream.print(string);
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException("End of input");
		}
		return Integer.parseInt(line);
	}

	/**
	 * @param map
	 *            The map we're dealing with
	 * @param reader
	 *            The stream we're reading from
	 * @param ostream
	 *            The stream we write the prompts to
	 * @return The tile the user specifies.
	 * @throws IOException
	 *             on I/O error
	 */
	private static Tile selectTile(final IMap map,
			final BufferedReader reader, final PrintStream ostream)
			throws IOException {
		return map.getTile(getInteger(reader, ostream, "Row: "),
				getInteger(reader, ostream, "Column: "));
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
	 * A map from commands to tables. Hunters, fishermen, and Food Gatherers are
	 * handled the same way, but with different tables.
	 */
	private static final Map<Character, String> ORDER_MAP = setUpOrders();
	/**
	 * Set up the map from commands to tables.
	 * @return the map
	 */
	@SuppressWarnings("boxing")
	private static Map<Character, String> setUpOrders() {
		final Map<Character, String> map = new HashMap<Character, String>();
		map.put('h', "hunter");
		map.put('H', "hunter");
		map.put('g', "gatherer");
		map.put('G', "gatherer");
		map.put('i', "fisher");
		map.put('I', "fisher");
		return map;
	}

	/**
	 * Repeatedly consult a table.
	 * 
	 * @param table
	 *            the table to consult
	 * @param tile
	 *            the tile to refer to
	 * @param reps
	 *            how many times to consult it
	 * @param ostream
	 *            the stream to print the results to
	 */
	private void repeatedlyConsultTable(final String table, final Tile tile,
			final int reps, final PrintStream ostream) {
		for (int i = 0; i < reps; i++) {
			try {
				ostream.println(runner.recursiveConsultTable(table, tile));
			} catch (MissingTableException e) {
				LOGGER.log(Level.SEVERE, "Missing table", e);
			}
		}
	}

	/**
	 * @param args
	 *            command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new ExplorationCLI().repl(new MapReaderAdapter().readMap(args[0],
					new Warning(Warning.Action.Warn)), new BufferedReader(
					new InputStreamReader(System.in)), SystemOut.SYS_OUT);
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			DriverQuit.quit(1);
			return; // NOPMD;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			DriverQuit.quit(2);
			return; // NOPMD;
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE, "Map contains invalid data", e);
			DriverQuit.quit(2);
			return; // NOPMD;
		}
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "ExplorationCLI";
	}
}
