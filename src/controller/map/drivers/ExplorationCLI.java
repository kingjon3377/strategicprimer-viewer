package controller.map.drivers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.IMap;
import model.map.MapDimensions;
import model.map.MapView;
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
import model.misc.IDriverModel;
import util.Pair;
import util.Warning;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapHelper;
import controller.map.misc.MapReaderAdapter;

/**
 * A CLI to help running exploration. TODO: Some of this should be made more
 * usable from other UIs.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationCLI implements ISPDriver {
	/**
	 * Find a fixture's location in the master map.
	 *
	 * @param fix the fixture to find.
	 * @param model the map model
	 * @return the first location found (search order is not defined) containing a
	 *         fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	public Point find(final TileFixture fix, final IDriverModel model) {
		final IMap source = model.getMap();
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
	 * @param model the exploration-model to use.
	 * @param unit the unit to move
	 * @param point the starting location
	 * @param direction the direction to move
	 * @return the movement cost
	 * @throws TraversalImpossibleException if movement in that direction is
	 *         impossible
	 */
	public int move(final IExplorationModel model, final Unit unit, final Point point,
			final Direction direction) throws TraversalImpossibleException {
		final Point dest = getDestination(model.getMapDimensions(), point, direction);
		// ESCA-JAVA0177:
		final int retval; //NOPMD
		final Tile destTile = model.getMap().getTile(dest);
		try {
			retval = SimpleMovement.getMovementCost(destTile);
		} catch (final TraversalImpossibleException except) {
			for (Pair<IMap, String> pair : model.getSubordinateMaps()) {
				final IMap map = pair.first();
				if (map.getTile(dest).isEmpty()) {
					map.getTiles().addTile(
							new Tile(dest.row, dest.col, destTile.getTerrain())); // NOPMD
				}
			}
			throw except;
		}
		model.getMap().getTile(point).removeFixture(unit);
		destTile.addFixture(unit);
		for (Pair<IMap, String> pair : model.getSubordinateMaps()) {
			final IMap map = pair.first();
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
				dtile = new Tile(dest.row, dest.col, destTile.getTerrain()); // NOPMD
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
	 * @param dims the dimensions of the map
	 * @param point a point
	 * @param direction a direction
	 * @return the point one tile in that direction.
	 */
	private static Point getDestination(final MapDimensions dims, final Point point, final Direction direction) {
		switch (direction) {
		case East:
			return PointFactory.point(point.row, // NOPMD
					increment(point.col, dims.cols - 1));
		case North:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					point.col);
		case Northeast:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					increment(point.col, dims.rows - 1));
		case Northwest:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					decrement(point.col, dims.cols - 1));
		case South:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					point.col);
		case Southeast:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					increment(point.col, dims.cols - 1));
		case Southwest:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					decrement(point.col, dims.cols - 1));
		case West:
			return PointFactory.point(point.row, // NOPMD
					decrement(point.col, dims.cols - 1));
		default:
			throw new IllegalStateException("Unhandled case");
		}
	}
	/**
	 * Driver. Takes as its parameters the map files to use.
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)  {
		try {
			new ExplorationCLI().startDriver(args);
		} catch (DriverFailedException except) {
			System.err.print(except.getMessage());
			System.err.println(':');
			System.err.println(except.getCause().getLocalizedMessage());
		}
	}
	/**
	 * TODO: Move much of this logic into class methods, so we don't need as many parameters.
	 * @param model the exploration-model to use.
	 * @param helper the helper to use to ask the user for directions.
	 * @param unit the unit in motion
	 * @param totalMP the unit's total MP (to start with)
	 * @throws IOException on I/O error getting input
	 */
	private void movementREPL(final IExplorationModel model,
			final MapHelper helper, final Unit unit, final int totalMP)
			throws IOException {
		int movement = totalMP;
		// "constants" is the fixtures that *always* get copied (e.g. forests,
		// mountains, hills, rivers). Also the player's own fortresses, so we'll
		// always see when we want to stop.
		while (movement > 0) {
			SystemOut.SYS_OUT.printC(movement).printC(" MP of ")
					.printC(totalMP).println(" remaining.");
			SystemOut.SYS_OUT
					.print("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ");
			SystemOut.SYS_OUT.println("6 = W, 7 = NW, 8 = Quit.");
			int cost = 0;
			cost = movementAtom(model, helper, unit);
			movement -= cost;
		}
	}

	/**
	 * The stuff from the loop of the movementREPL.
	 *
	 * @param model the exploration model to use
	 * @param helper the helper to use to ask the user for directions.
	 * @param unit the unit in motion
	 * @return the cost of the specified movement, 1 if not possible (in that
	 *         case we add the tile but no fixtures), or MAX_INT if "exit".
	 * @throws IOException on I/O error
	 */
	private int movementAtom(final IExplorationModel model,
			final MapHelper helper, final Unit unit) throws IOException {
		int cost;
		final List<TileFixture> allFixtures = new ArrayList<TileFixture>();
		final List<TileFixture> constants = new ArrayList<TileFixture>();
		final int directionNum = helper.inputNumber("Direction to move: ");
		if (directionNum > 7) {
			return Integer.MAX_VALUE; // NOPMD
		}
		final Direction direction = Direction.values()[directionNum];
		final Point point = find(unit, model);
		try {
			cost = move(model, unit, point, direction);
		} catch (TraversalImpossibleException except) {
			SystemOut.SYS_OUT.printC(
					"That direction is impassable; we've made sure ").println(
					"all maps show that at a cost of 1 MP");
			return 1; // NOPMD
		}
		final Point dPoint = getDestination(model.getMapDimensions(), point, direction);
		for (TileFixture fix : model.getMap().getTile(dPoint)) {
			if (shouldAlwaysNotice(unit, fix)) {
				constants.add(fix);
			} else if (mightNotice(unit, fix)) {
				allFixtures.add(fix);
			}
		}
		SystemOut.SYS_OUT.printC("The explorer comes to ")
				.printC(dPoint.toString()).printC(", a tile with terrain ")
				.println(model.getMap().getTile(dPoint).getTerrain());
		if (allFixtures.isEmpty()) {
			SystemOut.SYS_OUT
					.println("The following fixtures were automatically noticed:");
		} else {
			SystemOut.SYS_OUT.printC(
					"The following fixtures were noticed, all but the ")
					.println("last automtically:");
			Collections.shuffle(allFixtures);
			constants.add(allFixtures.get(0));
		}
		for (TileFixture fix : constants) {
			SystemOut.SYS_OUT.println(fix);
			for (Pair<IMap, String> pair : model.getSubordinateMaps()) {
				final IMap map = pair.first();
				map.getTile(dPoint).addFixture(fix);
			}
		}
		return cost;
	}

	/**
	 * FIXME: *Some* explorers *would* notice even unexposed ground.
	 *
	 * @param unit a unit
	 * @param fix a fixture
	 * @return whether the unit might notice it. Units do not notice themselves,
	 *         and do not notice unexposed ground.
	 */
	private static boolean mightNotice(final Unit unit, final TileFixture fix) {
		return (fix instanceof Ground && ((Ground) fix).isExposed())
				|| !(fix instanceof Ground || fix.equals(unit));
	}

	/**
	 * @param unit a unit
	 * @param fix a fixture
	 * @return whether the unit should always notice it.
	 */
	private static boolean shouldAlwaysNotice(final Unit unit, final TileFixture fix) {
		return fix instanceof Mountain
				|| fix instanceof RiverFixture
				|| fix instanceof Hill
				|| fix instanceof Forest
				|| (fix instanceof Fortress && ((Fortress) fix).getOwner()
						.equals(unit.getOwner()));
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
	/**
	 * Read maps.
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	private static IExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final MapView master = reader.readMap(filenames[0], Warning.INSTANCE);
		final IExplorationModel model = new ExplorationModel(master, filenames[0]);
		for (final String filename : filenames) {
			if (filename.equals(filenames[0])) {
				continue;
			}
			final IMap map = reader.readMap(filename, Warning.INSTANCE);
			if (!map.getDimensions().equals(master.getDimensions())) {
				throw new IllegalArgumentException("Size mismatch between " + filenames[0] + " and " + filename);
			}
			model.addSubordinateMap(map, filename);
		}
		return model;
	}

	/**
	 * Run the driver.
	 * @param args the command-line arguments
	 * @throws DriverFailedException on error.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: ExplorationCLI master-map [player-map ...]");
			System.exit(1);
		}
		final MapHelper helper = new MapHelper();
		final IExplorationModel model;
		try {
			model = readMaps(args);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP format error in map file", except);
		}
		final List<Player> players = model.getPlayerChoices();
		try {
			final int playerNum = helper.chooseFromList(players,
					"The players shared by all the maps:",
					"No players shared by all the maps.",
					"Please make a selection: ", true);
			if (playerNum < 0) {
				return; // NOPMD
			}
			final Player player = players.get(playerNum);
			final List<Unit> units = model.getUnits(player);
			final int unitNum = helper.chooseFromList(units, "Player's units:",
					"That player has no units in the master map.",
					"Please make a selection: ", true);
			if (unitNum < 0) {
				return; // NOPMD
			}
			final Unit unit = units.get(unitNum);
			SystemOut.SYS_OUT.println("Details of that unit:");
			SystemOut.SYS_OUT.println(unit.verbose());
			movementREPL(model, helper, unit,
					helper.inputNumber("MP that unit has: "));
		} catch (IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
		try {
			writeMaps(model);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error writing to a map file", except);
		}
	}
	/**
	 * Write maps to disk.
	 * @param model the model containing all the maps
	 * @throws IOException on I/O error
	 */
	private static void writeMaps(final IExplorationModel model) throws IOException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (Pair<IMap, String> pair : model.getAllMaps()) {
			reader.write(pair.second(), pair.first());
		}
	}
}
