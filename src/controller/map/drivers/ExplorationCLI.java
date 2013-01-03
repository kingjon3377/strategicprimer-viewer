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
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;
import util.IsNumeric;
import view.util.SystemOut;
import controller.map.SPFormatException;
import controller.map.misc.MapHelper;

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
			if (map.getDimensions().equals(master.getDimensions())) {
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
	 * map. If movement in the specified direction is impossible, we update all
	 * subordinate maps with the terrain information showing that, then re-throw
	 * the exception; callers should deduct a minimal MP cost.
	 *
	 * @param unit the unit to move
	 * @param point the starting location
	 * @param direction the direction to move
	 * @return the movement cost
	 * @throws TraversalImpossibleException if movement in that direction is
	 *         impossible
	 */
	public int move(final Unit unit, final Point point,
			final Direction direction) throws TraversalImpossibleException {
		final Point dest = getDestination(point, direction);
		// ESCA-JAVA0177:
		final int retval; //NOPMD
		try {
			retval = SimpleMovement.getMovementCost(source.getTile(dest));
		} catch (final TraversalImpossibleException except) {
			for (IMap map : dests) {
				if (map.getTile(dest).isEmpty()) {
					map.getTiles().addTile(
							new Tile(dest.row, dest.col, source.getTile(dest)//NOPMD
									.getTerrain()));
				}
			}
			throw except;
		}
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
	 * A "plus one" method with a configurable, low "overflow".
	 * @param num the number to increment
	 * @param max the maximum number we want to return
	 * @return either num + 1, if max or lower, or 0.
	 */
	public static int increment(final int num, final int max) {
		return num >= max - 1 ? 0 : num + 1;
	}
	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low value.
	 * @param num the number to decrement.
	 * @param max the number to "underflow" to.
	 * @return either num - 1, if 1 or higher, or max.
	 */
	public static int decrement(final int num, final int max) {
		return num == 0 ? max : num - 1;
	}
	/**
	 * @param point a point
	 * @param direction a direction
	 * @return the point one tile in that direction.
	 */
	private Point getDestination(final Point point, final Direction direction) {
		switch (direction) {
		case East:
			return PointFactory.point(point.row, // NOPMD
					increment(point.col, source.getDimensions().cols - 1));
		case North:
			return PointFactory.point(decrement(point.row, source.getDimensions().rows - 1), // NOPMD
					point.col);
		case Northeast:
			return PointFactory.point(decrement(point.row, source.getDimensions().rows - 1), // NOPMD
					increment(point.col, source.getDimensions().rows - 1));
		case Northwest:
			return PointFactory.point(decrement(point.row, source.getDimensions().rows - 1), // NOPMD
					decrement(point.col, source.getDimensions().cols - 1));
		case South:
			return PointFactory.point(increment(point.row, source.getDimensions().rows - 1), // NOPMD
					point.col);
		case Southeast:
			return PointFactory.point(increment(point.row, source.getDimensions().rows - 1), // NOPMD
					increment(point.col, source.getDimensions().cols - 1));
		case Southwest:
			return PointFactory.point(increment(point.row, source.getDimensions().rows - 1), // NOPMD
					decrement(point.col, source.getDimensions().cols - 1));
		case West:
			return PointFactory.point(point.row, // NOPMD
					decrement(point.col, source.getDimensions().cols - 1));
		default:
			throw new IllegalStateException("Unhandled case");
		}
	}
	/**
	 * Driver. Takes as its parameters the map files to use.
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)  {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: ExplorationCLI master-map [player-map ...]");
			System.exit(1);
		}
		final List<IMap> secondaries = new ArrayList<IMap>();
		final List<IMap> maps = new ArrayList<IMap>();
		final MapHelper helper = new MapHelper();
		IMap master;
		try {
			master = helper.readMaps(args, maps, secondaries);
		} catch (IOException except) {
			System.err.println("I/O error reading maps:");
			System.err.println(except.getLocalizedMessage());
			System.exit(1);
			return; // NOPMD
		} catch (XMLStreamException except) {
			System.err.println("Malformed XML in map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(2);
			return; // NOPMD
		} catch (SPFormatException except) {
			System.err.println("SP format error in map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(3);
			return; // NOPMD
		}
		final ExplorationCLI cli = new ExplorationCLI(master, secondaries);
		final List<Player> players = helper.getPlayerChoices(maps);
		try {
			final int playerNum = helper.chooseFromList(players,
					"The players shared by all the maps:",
					"No players shared by all the maps.",
					"Please make a selection: ");
			if (playerNum < 0) {
				return; // NOPMD
			}
			final Player player = players.get(playerNum);
			final List<Unit> units = helper.getUnits(master, player);
			final int unitNum = helper.chooseFromList(units, "Player's units:",
					"That player has no units in the master map.",
					"Please make a selection: ");
			if (unitNum < 0) {
				return; // NOPMD
			}
			final Unit unit = units.get(unitNum);
			SystemOut.SYS_OUT.println("Details of that unit:");
			SystemOut.SYS_OUT.println(unit.verbose());
			movementREPL(secondaries, master, cli, unit,
					inputNumber("MP that unit has: "));
		} catch (IOException except) {
			System.exit(4);
			return; // NOPMD
		}
		try {
			helper.writeMaps(maps, args);
		} catch (IOException except) {
			System.err.println("I/O error writing to a map file:");
			System.err.println(except.getLocalizedMessage());
			System.exit(5);
		}
	}
	/**
	 * @param secondaries the maps to update with data from the master map
	 * @param master the main map
	 * @param cli the object that does the moving of the unit
	 * @param unit the unit in motion
	 * @param totalMP the unit's total MP (to start with)
	 * @throws IOException on I/O error getting input
	 */
	private static void movementREPL(final List<IMap> secondaries,
			final IMap master, final ExplorationCLI cli, final Unit unit,
			final int totalMP) throws IOException {
		int movement = totalMP;
		final List<TileFixture> allFixtures = new ArrayList<TileFixture>();
		// "constants" is the fixtures that *always* get copied (e.g. forests,
		// mountains, hills, rivers). Also the player's own fortresses, so we'll
		// always see when we want to stop.
		final List<TileFixture> constants = new ArrayList<TileFixture>();
		while (movement > 0) {
			allFixtures.clear();
			constants.clear();
			SystemOut.SYS_OUT.printC(movement).printC(" MP of ")
					.printC(totalMP).println(" remaining.");
			SystemOut.SYS_OUT.print("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ");
			SystemOut.SYS_OUT.println("6 = W, 7 = NW, 8 = Quit.");
			final int directionNum = inputNumber("Direction to move: ");
			if (directionNum > 7) {
				break;
			}
			final Direction direction = Direction.values()[directionNum];
			final Point point = cli.find(unit);
			try {
				final int cost = cli.move(unit, point, direction);
				movement -= cost;
			} catch (TraversalImpossibleException except) {
				movement--;
				SystemOut.SYS_OUT.printC(
						"That direction is impassable; we've made sure ")
						.println("all maps show that at a cost of 1 MP");
				continue;
			}
			final Point dPoint = cli.getDestination(point, direction);
			for (TileFixture fix : master.getTile(dPoint)) {
				if (fix instanceof Mountain || fix instanceof RiverFixture
						|| fix instanceof Hill || fix instanceof Forest
						|| (fix instanceof Fortress && ((Fortress) fix)
								.getOwner().equals(unit.getOwner()))) {
					constants.add(fix);
				} else if ((fix instanceof Ground && ((Ground) fix).isExposed())
						|| !(fix instanceof Ground || fix.equals(unit))) {
					// FIXME: *Some* explorers would notice even unexposed ground.
					allFixtures.add(fix);
				}
			}
			SystemOut.SYS_OUT.printC("The explorer comes to ")
					.printC(dPoint.toString()).printC(", a tile with terrain ")
					.println(master.getTile(dPoint).getTerrain());
			if (allFixtures.isEmpty()) {
				SystemOut.SYS_OUT.println("The following fixtures were automatically noticed:");
			} else {
				SystemOut.SYS_OUT.printC(
						"The following fixtures were noticed, all but the")
						.println("last automtically:");
				Collections.shuffle(allFixtures);
				constants.add(allFixtures.get(0));
			}
			for (TileFixture fix : constants) {
				SystemOut.SYS_OUT.println(fix);
				for (IMap map : secondaries) {
					map.getTile(dPoint).addFixture(fix);
				}
			}
		}
	}
	/**
	 * Read input from stdin repeatedly until a nonnegative integer is entered, and return it.
	 * @param prompt The prompt to prompt the user with
	 * @return the number entered
	 * @throws IOException on I/O error
	 */
	public static int inputNumber(final String prompt) throws IOException {
		int retval = -1;
		final BufferedReader istream = new BufferedReader(new InputStreamReader(System.in));
		while (retval < 0) {
			SystemOut.SYS_OUT.print(prompt);
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
		Northwest;
	}
}
