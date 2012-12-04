package controller.map.drivers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Player;
import model.map.Point;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.mobile.Unit;
import util.IsNumeric;
import util.Warning;
import view.util.SystemOut;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A CLI to help running exploration. TODO: Some of this should be made more
 * usable from other UIs.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationCLI {
	/**
	 * Constructor.
	 *
	 * @param master The master map.
	 * @param secondaries Any player maps that should be updated with results of
	 *        exploration.
	 */
	public ExplorationCLI(final IMap master, final List<IMap> secondaries) {
		source = master;
		for (IMap map : secondaries) {
			if (map.rows() == master.rows() && map.cols() == master.cols()) {
				dests.add(map);
			} else {
				throw new IllegalArgumentException("Size mismatch");
			}
		}
	}
	/**
	 * The source map, which we'll get data from but only update to move the moving unit.
	 */
	private final IMap source;
	/**
	 * The destination maps, which will be updated with things a moving unit sees.
	 */
	private final List<IMap> dests = new ArrayList<IMap>();
	/**
	 * Given a list of maps, return a list (also a set, but we won't guarantee
	 * that) of players listed in all of them. TODO: move to a more general
	 * utility class?
	 * @param maps the maps to consider
	 * @return the list of players
	 */
	public static List<Player> getPlayerChoices(final List<IMap> maps) {
		if (maps.isEmpty()) {
			throw new IllegalArgumentException("Need at least one map.");
		}
		final List<Player> retval = new ArrayList<Player>();
		for (Player player : maps.get(0).getPlayers()) {
			retval.add(player);
		}
		for (IMap map : maps) {
			retval.retainAll(map.getPlayers());
		}
		return retval;
	}
	/**
	 * @param map a map
	 * @param player a player
	 * @return a list of all units in the map belonging to that player.
	 */
	public static List<Unit> getUnits(final IMap map, final Player player) {
		final List<Unit> retval = new ArrayList<Unit>();
		for (final Point point : map.getTiles()) {
			final Tile tile = map.getTile(point);
			for (final TileFixture fix : tile) {
				if (fix instanceof Unit && ((Unit) fix).getOwner().equals(player)) {
					retval.add((Unit) fix);
				}
			}
		}
		return retval;
	}
	/**
	 * Driver. Takes as its parameters the map files to use.
	 * @param args the command-line arguments
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	public static void main(final String[] args) throws IOException,
			XMLStreamException, SPFormatException {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: ExplorationCLI master-map [player-map ...]");
			System.exit(1);
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final IMap master = reader.readMap(args[0], Warning.INSTANCE);
		final List<IMap> secondaries = new ArrayList<IMap>();
		final List<IMap> maps = new ArrayList<IMap>(
				Collections.singletonList(master));
		for (int i = 1; i < args.length; i++) {
			final IMap map = reader.readMap(args[i], Warning.INSTANCE);
			secondaries.add(map);
			maps.add(map);
		}
		final ExplorationCLI cli = new ExplorationCLI(master, secondaries);
		final List<Player> players = getPlayerChoices(maps);
		if (players.isEmpty()) {
			SystemOut.SYS_OUT.println("No players shared by all the maps.");
			return;
		}
		SystemOut.SYS_OUT.println("The players shared by all the maps:");
		for (int i = 0; i < players.size(); i++) {
			SystemOut.SYS_OUT.print(i);
			SystemOut.SYS_OUT.print(": ");
			SystemOut.SYS_OUT.println(players.get(i).getName());
		}
		Player player = players.get(inputNumber());
		final List<Unit> units = getUnits(master, player);
		if (units.isEmpty()) {
			SystemOut.SYS_OUT.println("That player has no units in the master map.");
			return;
		}
		SystemOut.SYS_OUT.println("Player's units:");
		for (int i = 0; i < units.size(); i++) {
			SystemOut.SYS_OUT.print(i);
			SystemOut.SYS_OUT.print(": ");
			SystemOut.SYS_OUT.println(units.get(i).getName());
		}
		Unit unit = units.get(inputNumber());
		SystemOut.SYS_OUT.println("Details of that unit:");
		SystemOut.SYS_OUT.println(unit.verbose());
	}
	/**
	 * Read input from stdin repeatedly until a nonnegative integer is entered, and return it.
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	public static int inputNumber() throws IOException {
		int retval = -1;
		final BufferedReader istream = new BufferedReader(new InputStreamReader(System.in));
		while (retval < 0) {
			SystemOut.SYS_OUT.print("Please make a selection: ");
			final String input = istream.readLine();
			if (IsNumeric.isNumeric(input)) {
				retval = Integer.parseInt(input);
			}
		}
		return retval;
	}
}
