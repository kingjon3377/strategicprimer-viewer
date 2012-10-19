package controller.map.drivers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.PointFactory;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.terrain.Forest;
import util.Warning;
import view.util.DriverQuit;
import view.util.SystemOut;
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
	 * Constructor.
	 */
	private ExplorationCLI() {
		// Not for general use.
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
			while (input != null && input.length() > 0 && input.charAt(0) != 'q') {
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
	 * @param map the map
	 * @param reader the stream to read further input from
	 * @param ostream the stream to write to
	 * @param input the command
	 *
	 * @throws IOException on I/O error
	 */
	public void handleCommand(final IMap map, final BufferedReader reader,
			final PrintStream ostream, final char input) throws IOException {
		switch (input) {
		case 'x':
			explore(map, reader, ostream);
			break;
		case 'f':
			fortressInfo(selectTile(map, reader, ostream), ostream);
			break;
		case 'h':
		case 'i':
			hunt(populateList(selectTile(map, reader, ostream)), ostream,
					HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		case 'g':
			gather(populateList(selectTile(map, reader, ostream)), ostream,
					HUNTER_HOURS * HOURLY_ENCOUNTERS);
			break;
		default:
			ostream.println("Unknown command.");
			break;
		}
	}
	/**
	 * Adapter between Tile and List<TileFixture>.
	 * @param tile a tile
	 * @return a list of the fixtures on it.
	 */
	private static List<TileFixture> populateList(final Tile tile) {
		final List<TileFixture> retval = new ArrayList<TileFixture>();
		for (TileFixture fix : tile) {
			retval.add(fix);
		}
		return retval;
	}
	/**
	 * Run hunting, fishing, or trapping.
	 * @param fixtures a list of the fixtures on the tile
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 */
	private static void hunt(final List<TileFixture> fixtures,
			final PrintStream ostream, final int encounters) {
		for (int i = 0; i < encounters; i++) {
			Collections.shuffle(fixtures);
			final TileFixture fix = fixtures.get(0);
			if (fix instanceof Animal) {
				ostream.println(fix);
			} else {
				ostream.print("nothing ... (");
				ostream.print(fix.getClass().getName());
				ostream.println(')');
			}
		}
	}
	/**
	 * Run food-gathering.
	 * @param fixtures a list of the fixtures on the tile
	 * @param ostream the stream to write to
	 * @param encounters how many encounters to show
	 */
	private static void gather(final List<TileFixture> fixtures,
			final PrintStream ostream, final int encounters) {
		for (int i = 0; i < encounters; i++) {
			Collections.shuffle(fixtures);
			final TileFixture fix = fixtures.get(0);
			if (fix instanceof Grove || fix instanceof Meadow) {
				ostream.println(fix);
			} else {
				ostream.print("nothing ... (");
				ostream.print(fix.getClass().getName());
				ostream.println(')');
			}
		}
	}
	/**
	 * Explore a user-specified tile.
	 *
	 * @param map the map
	 * @param reader source of user input
	 * @param ostream the stream to write the results to
	 *
	 * @throws IOException on I/O error
	 */
	private static void explore(final IMap map, final BufferedReader reader,
			final PrintStream ostream) throws IOException {
		final Tile tile = selectTile(map, reader, ostream);
		ostream.print("Tile is ");
		ostream.println(tile.getTerrain());
		ostream.print("Fixtures influencing terrain: ");
		boolean any = false;
		final List<TileFixture> fixtures = new ArrayList<TileFixture>();
		for (final TileFixture fix : tile) {
			if (fix instanceof TerrainFixture) {
				if (any) {
					ostream.print("; ");
				} else {
					any = true;
				}
				ostream.print(fix);
			}
			fixtures.add(fix);
		}
		ostream.println();
		ostream.print("Here are four random fixtures from the tile:");
		Collections.shuffle(fixtures);
		for (int i = 0; i < 4; i++) {
			ostream.println(fixtures.get(i));
		}
	}

	/**
	 * Give the data the player automatically knows about a user-specified tile
	 * if he has a fortress on it.
	 *
	 * @param tile the selected tile
	 * @param ostream the stream to print results to
	 */
	private static void fortressInfo(final Tile tile, final PrintStream ostream) {
		ostream.print("Terrain is ");
		ostream.println(tile.getTerrain());
		final List<TileFixture> fixtures = populateList(tile);
		final List<Ground> ground = new ArrayList<Ground>();
		final List<Forest> forests = new ArrayList<Forest>();
		for (TileFixture fix : fixtures) {
			if (fix instanceof Ground) {
				ground.add((Ground) fix);
			} else if (fix instanceof Forest) {
				forests.add((Forest) fix);
			}
		}
		if (!ground.isEmpty()) {
			ostream.println("Kind(s) of ground (rock) on the tile:");
			for (Ground fix : ground) {
				ostream.println(fix);
			}
		}
		if (!forests.isEmpty()) {
			ostream.println("Kind(s) of forests on the tile:");
			for (Forest fix : forests) {
				ostream.println(fix);
			}
		}
	}

	/**
	 * @param reader the stream we read from
	 * @param ostream the stream we write to
	 * @param string the prompt
	 * @return the integer the player specified
	 * @throws IOException on I/O error
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
	 * @param map The map we're dealing with
	 * @param reader The stream we're reading from
	 * @param ostream The stream we write the prompts to
	 * @return The tile the user specifies.
	 * @throws IOException on I/O error
	 */
	private static Tile selectTile(final IMap map, final BufferedReader reader,
			final PrintStream ostream) throws IOException {
		return map.getTile(PointFactory.point(
				getInteger(reader, ostream, "Row: "),
				getInteger(reader, ostream, "Column: ")));
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
	 * @param args command-line arguments
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
