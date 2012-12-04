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
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;
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
	 * Find a fixture's location in the master map.
	 *
	 * @param fix the fixture to find.
	 * @return the first location found (search order is not defined) containing a
	 *         fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	public Point find(final TileFixture fix) {
		for (Point point : source.getTiles()) {
			for (TileFixture item : source.getTile(point)) {
				if (fix.equals(item)) {
					return point; // NOPMD
				}
			}
		}
		return PointFactory.point(-1, -1);
	}

	/**
	 * Move a unit from the specified tile one tile in the specified direction.
	 * Moves the unit in all maps where the unit *was* in the specified tile,
	 * copying terrain information if the tile didn't exist in a subordinate
	 * map.
	 *
	 * @param unit the unit to move
	 * @param point the starting location
	 * @param direction the direction to move
	 * @return the movement cost
	 * @throws TraversalImpossibleException if movement in that direction is impossible
	 */
	public int move(final Unit unit, final Point point,
			final Direction direction) throws TraversalImpossibleException {
		final Point dest = getDestination(point, direction);
		final int retval = SimpleMovement.getMovementCost(source.getTile(dest));
		source.getTile(point).removeFixture(unit);
		source.getTile(dest).addFixture(unit);
		for (IMap map : dests) {
			final Tile stile = map.getTile(point);
			boolean hasUnit = false;
			for (final TileFixture fix : stile) {
				if (fix.equals(unit)) {
					hasUnit = true;
					break;
				}
			}
			if (!hasUnit) {
				continue;
			}
			Tile dtile = map.getTile(dest);
			if (dtile.isEmpty()) {
				dtile = new Tile(dest.row, dest.col, source.getTile(dest) // NOPMD
						.getTerrain());
				map.getTiles().addTile(dtile);
			}
			stile.removeFixture(unit);
			dtile.addFixture(unit);
		}
		return retval;
	}
	/**
	 * @param point a point
	 * @param direction a direction
	 * @return the point one tile in that direction.
	 */
	private Point getDestination(final Point point, final Direction direction) {
		switch (direction) {
		case East:
			return PointFactory.point(point.row, point.col + 1); // NOPMD
		case North:
			return PointFactory.point(point.row - 1, point.col); // NOPMD
		case Northeast:
			return PointFactory.point(point.row - 1, point.col + 1); // NOPMD
		case Northwest:
			return PointFactory.point(point.row - 1, point.col - 1); // NOPMD
		case South:
			return PointFactory.point(point.row + 1, point.col); // NOPMD
		case Southeast:
			return PointFactory.point(point.row + 1, point.col + 1); // NOPMD
		case Southwest:
			return PointFactory.point(point.row + 1, point.col - 1); // NOPMD
		case Stop:
			throw new IllegalArgumentException("Stop means no destination.");
		case West:
			return PointFactory.point(point.row, point.col - 1); // NOPMD
		default:
			throw new IllegalStateException("Unhandled case");
		}
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
			return; // NOPMD
		}
		SystemOut.SYS_OUT.println("The players shared by all the maps:");
		for (int i = 0; i < players.size(); i++) {
			SystemOut.SYS_OUT.print(i);
			SystemOut.SYS_OUT.print(": ");
			SystemOut.SYS_OUT.println(players.get(i).getName());
		}
		final Player player = players.get(inputNumber());
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
		final Unit unit = units.get(inputNumber());
		SystemOut.SYS_OUT.println("Details of that unit:");
		SystemOut.SYS_OUT.println(unit.verbose());
		SystemOut.SYS_OUT.print("MP that unit has: ");
		int movement = inputNumber();
		int direction = -1;
		final List<TileFixture> allFixtures = new ArrayList<TileFixture>();
		// "contstants" is the fixtures that *always* get copied, such as
		// forests, mountains hills, and rivers.
		final List<TileFixture> constants = new ArrayList<TileFixture>();
		while (movement > 0) {
			SystemOut.SYS_OUT.print("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ");
			SystemOut.SYS_OUT.println("6 = W, 7 = NW, 8 = Quit.");
			SystemOut.SYS_OUT.print("Direction to move: ");
			direction = inputNumber();
			if (direction > 7) {
				break;
			}
			final Point point = cli.find(unit);
			final Point dPoint = cli.getDestination(point, Direction.values()[direction]);
			// ESCA-JAVA0177:
			int cost;
			try {
				cost = cli.move(unit, point, Direction.values()[direction]);
			} catch (TraversalImpossibleException except) {
				movement--;
				for (IMap map : secondaries) {
					if (map.getTile(dPoint).isEmpty()) {
						map.getTiles().addTile(
								new Tile(dPoint.row, dPoint.col, master// NOPMD
										.getTile(dPoint).getTerrain()));
					}
				}
				SystemOut.SYS_OUT.println("That direction is impassable; we've made sure all maps show that at a cost of 1 MP");
				continue;
			}
			movement -= cost;
			allFixtures.clear();
			// "contstants" is the fixtures that *always* get copied, such as
			// forests, mountains hills, and rivers. Also fortresses, primarily
			// so we'll always see when we want to stop. FIXME: Find a better way
			// of doing that, so players can have a strategy based on secrecy.
			constants.clear();
			for (TileFixture fix : master.getTile(dPoint)) {
				allFixtures.add(fix);
				if (fix instanceof Mountain || fix instanceof RiverFixture
						|| fix instanceof Hill || fix instanceof Forest
						|| fix instanceof Fortress) {
					constants.add(fix);
				}
			}
			SystemOut.SYS_OUT.print("The explorer comes to a tile with terrain ");
			SystemOut.SYS_OUT.print(master.getTile(dPoint).getTerrain());
			SystemOut.SYS_OUT.println(" and the following automatically-noticed fixtures:");
			for (TileFixture fix : constants) {
				SystemOut.SYS_OUT.println(fix);
			}
			Collections.shuffle(allFixtures);
			if (!allFixtures.isEmpty()) {
				SystemOut.SYS_OUT.println("The following fixture was also noticed:");
				SystemOut.SYS_OUT.println(allFixtures.get(0));
			}
			SystemOut.SYS_OUT.println("Updating subordinate maps with new data ...");
			for (IMap map : secondaries) {
				for (final TileFixture fix : constants) {
					map.getTile(dPoint).addFixture(fix);
				}
				if (!allFixtures.isEmpty()) {
					map.getTile(dPoint).addFixture(allFixtures.get(0));
				}
			}
		}
		for (int i = 1; i < args.length; i++) {
			reader.write(args[i], maps.get(i));
		}
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
	/**
	 * An enumeration of directions.
	 */
	public enum Direction {
		/**
		 * North.
		 */
		North,
		/**
		 * Northeast.
		 */
		Northeast,
		/**
		 * East.
		 */
		East,
		/**
		 * Southeast.
		 */
		Southeast,
		/**
		 * South.
		 */
		South,
		/**
		 * Southwest.
		 */
		Southwest,
		/**
		 * West.
		 */
		West,
		/**
		 * Northwest.
		 */
		Northwest,
		/**
		 * Stop (i.e. quit).
		 */
		Stop;
	}
}
