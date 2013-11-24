package controller.map.converter; // NOPMD

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.exploration.old.ExplorationRunner;
import model.exploration.old.MissingTableException;
import model.map.IMap;
import model.map.ITile;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import model.workermgmt.RaceFactory;
import util.Pair;
import util.TypesafeLogger;
import util.Warning;
import controller.exploration.TableLoader;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.MapReaderAdapter;

/**
 * A class to convert a version-1 map to a version-2 map with greater
 * resolution.
 *
 * TODO: Write tests.
 *
 * @author Jonathan Lovelace
 *
 */
public class OneToTwoConverter { // NOPMD
	/**
	 * Constructor.
	 */
	public OneToTwoConverter() {
		TableLoader.loadAllTables("tables", runner);
	}

	/**
	 * Sixty percent. Our probability for a couple of perturbations.
	 */
	private static final double SIXTY_PERCENT = .6;
	/**
	 * The next turn. Use for TextFixtures to replace with generated encounters
	 * later.
	 */
	private static final int NEXT_TURN = 10;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(OneToTwoConverter.class);
	/**
	 * The number of subtiles per tile on each axis.
	 */
	private static final int RES_JUMP = 4;
	/**
	 * The maximum number of iterations per tile.
	 */
	private static final int MAX_ITERATIONS = 100;

	/**
	 * @param old a version-1 map
	 * @param main whether the map is the main map (new encounter-type fixtures
	 *        don't go on players' maps)
	 * @return a version-2 equivalent with greater resolution
	 */
	public SPMap convert(final IMap old, final boolean main) {
		final IDFactory idFactory = new IDFactory();
		final MapDimensions oldDim = old.getDimensions();
		final SPMap retval = new SPMap(new MapDimensions(
				oldDim.rows * RES_JUMP, oldDim.cols * RES_JUMP, 2));
		for (final Player player : old.getPlayers()) {
			if (player != null) {
				retval.addPlayer(player);
			}
		}
		final List<Pair<Point, ITile>> converted = new LinkedList<>();
		final Player independent = retval.getPlayers().getIndependent();
		for (int row = 0; row < oldDim.rows; row++) {
			for (int col = 0; col < oldDim.cols; col++) {
				final Point point = PointFactory.point(row, col);
				for (final Pair<Point, ITile> pair : convertTile(point,
						old.getTile(point), main, idFactory, independent)) {
					retval.addTile(pair.first(), pair.second());
					converted.add(pair);
				}
			}
		}
		final Random random = new Random(MAX_ITERATIONS);
		Collections.shuffle(converted, random);
		for (final Pair<Point, ITile> pair : converted) {
			perturb(pair.first(), pair.second(), retval, random, main,
					idFactory);
		}
		return retval;
	}

	/**
	 * Create the initial list of subtiles for a tile.
	 *
	 * @param tile the tile on the old map
	 * @param point its location
	 * @param main whether this is the main map or a player's map
	 * @return the equivalent higher-resolution tiles, in initial form
	 */
	private List<Pair<Point, ITile>> createInitialSubtiles(final Point point,
			final ITile tile, final boolean main) {
		final List<Pair<Point, ITile>> initial = new LinkedList<>();
		if (!tile.isEmpty()) {
			for (int i = 0; i < RES_JUMP; i++) {
				for (int j = 0; j < RES_JUMP; j++) {
					final int row = point.row * RES_JUMP + i;
					final int col = point.col * RES_JUMP + j;
					final Point subpoint = PointFactory.point(row, col);
					final ITile subtile = new Tile(tile.getTerrain()); // NOPMD
					initial.add(Pair.of(subpoint, subtile));
					convertSubtile(subpoint, subtile, main);
				}
			}
		}
		return initial;
	}

	/**
	 * @param tile a tile on the old map
	 * @param point its location
	 * @param main whether this is the main map or a player's map
	 * @param idFactory the IDFactory to use to get IDs.
	 * @param independentPlayer the Player to own villages
	 * @return the equivalent higher-resolution tiles.
	 */
	private List<Pair<Point, ITile>> convertTile(final Point point,
			final ITile tile, final boolean main, final IDFactory idFactory,
			final Player independentPlayer) {
		final List<Pair<Point, ITile>> initial = createInitialSubtiles(point,
				tile, main);
		if (!tile.isEmpty()) {
			final int id = idFactory.createID();
			tile.addFixture(new Village(TownStatus.Active, "", id,
					independentPlayer, RaceFactory.getRace(new Random(id))));
			final List<TileFixture> fixtures = new LinkedList<>();
			for (final TileFixture fixture : tile) {
				fixtures.add(fixture);
			}
			separateRivers(tile, initial, fixtures);
			final Random random = new Random(getSeed(point));
			Collections.shuffle(initial, random);
			Collections.shuffle(fixtures, random);
			int iterations = 0;
			while (iterations < MAX_ITERATIONS && !fixtures.isEmpty()) {
				if (isSubtileSuitable(initial.get(0).second())) {
					final TileFixture fix = fixtures.remove(0);
					assert fix != null;
					changeFor(initial.get(0).second(), fix);
					initial.get(0).second().addFixture(fix);
				}
				initial.add(initial.remove(0));
				iterations++;
			}
			if (iterations == MAX_ITERATIONS) {
				LOGGER.severe("Maximum number of iterations reached on tile ("
						+ point.row + ", " + point.col + "); forcing ...");
				while (!fixtures.isEmpty()) {
					final ITile subtile = initial.get(0).second();
					final TileFixture fix = fixtures.remove(0);
					assert fix != null;
					subtile.addFixture(fix);
					subtile.addFixture(new TextFixture(
							// NOPMD
							"FIXME: A fixture here was force-added after MAX_ITER",
							NEXT_TURN));
					initial.add(initial.remove(0));
				}
			}
		}
		return initial;
	}

	/**
	 * Deal with rivers separately.
	 *
	 * @param tile the tile being handled
	 * @param initial the initial set of subtiles
	 * @param fixtures the list of fixtures on the initial tile, to be parceled
	 *        out among the subtiles
	 */
	private static void separateRivers(final ITile tile,
			final List<Pair<Point, ITile>> initial,
			final List<TileFixture> fixtures) {
		if (tile.hasRiver()) {
			final RiverFixture rivers = tile.getRivers();
			for (final River river : rivers) {
				if (river != null) {
					addRiver(river, initial);
				}
			}
			fixtures.remove(rivers);
		}
	}

	/**
	 * An exploration runner, to get forest and ground types from.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();

	/**
	 * Convert a tile. That is, change it from a forest or mountain type to the
	 * proper replacement type plus the proper fixture. Also, in any case, add
	 * the proper Ground.
	 *
	 * @param tile the tile to convert
	 * @param point the location of the tile
	 * @param main whether this is the main map or a player's map
	 */
	@SuppressWarnings("deprecation")
	private void convertSubtile(final Point point, final ITile tile,
			final boolean main) {
		try {
			if (TileType.Mountain.equals(tile.getTerrain())) {
				tile.setTerrain(TileType.Plains);
				tile.addFixture(new Mountain());
			} else if (TileType.TemperateForest.equals(tile.getTerrain())) {
				if (!hasForest(tile)) {
					tile.addFixture(new Forest(runner.getPrimaryTree(point,
							tile), false));
				}
				tile.setTerrain(TileType.Plains);
			} else if (TileType.BorealForest.equals(tile.getTerrain())) {
				if (!hasForest(tile)) {
					tile.addFixture(new Forest(runner.getPrimaryTree(point,
							tile), false));
				}
				tile.setTerrain(TileType.Steppe);
			}
			addFixture(tile, new Ground(runner.getPrimaryRock(point, tile),
					false), main);
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing table", e);
		}
	}

	/**
	 * Determine whether a subtile is suitable for more fixtures. It's suitable
	 * if its only fixtures are Forests, Mountains, Ground or other similar
	 * "background".
	 *
	 * @param tile the tile
	 * @return whether it's suitable
	 */
	private static boolean isSubtileSuitable(final ITile tile) {
		for (final TileFixture fix : tile) {
			if (!(fix instanceof Forest || fix instanceof Mountain
					|| fix instanceof Ground || fix instanceof Sandbar
					|| fix instanceof Shrub || fix instanceof Meadow || fix instanceof Hill)) {
				return false; // NOPMD
			}
		}
		return true;
	}

	/**
	 * Prepare a subtile for a specified new fixture. At present, the only
	 * change this involves is removing any forests if there's a village or
	 * TownEvent.
	 *
	 * @param tile the tile to prepare
	 * @param fix the fixture to prepare it for
	 */
	private static void changeFor(final ITile tile, final TileFixture fix) {
		if (fix instanceof Village || fix instanceof ITownFixture) {
			final List<TileFixture> forests = new ArrayList<>();
			for (final TileFixture fixture : tile) {
				if (fixture instanceof Forest) {
					forests.add(fixture);
				}
			}
			for (final TileFixture fixture : forests) {
				if (fixture != null) {
					tile.removeFixture(fixture);
				}
			}
		}
	}

	/**
	 * Possibly make a random change to a tile.
	 *
	 * @param tile the tile under consideration
	 * @param point its location
	 * @param map the map it's on, so we can consider adjacent tiles
	 * @param random the source of randomness (so this is repeatable with
	 *        players' maps)
	 * @param main whether we should actually add the fixtures (i.e. is this the
	 *        main map)
	 * @param idFactory the factory to use to create ID numbers
	 */
	private void perturb(final Point point, final ITile tile, final IMap map,
			final Random random, final boolean main, final IDFactory idFactory) {
		if (!TileType.Ocean.equals(tile.getTerrain())) {
			if (isAdjacentToTown(point, map)
					&& random.nextDouble() < SIXTY_PERCENT) {
				addFieldOrOrchard(random.nextBoolean(), point, tile, main,
						idFactory);
			} else if (TileType.Desert.equals(tile.getTerrain())) {
				final boolean watered = isAdjacentToWater(point, map);
				waterDesert(tile, random, watered);
			} else if (random.nextDouble() < .1) {
				addForest(point, tile, main);
			}
		}
	}

	/**
	 * Make changes to a desert tile based on water.
	 *
	 * @param tile the tile
	 * @param random the source of randomness
	 * @param watered whether the tile is adjacent to water
	 */
	private static void waterDesert(final ITile tile, final Random random,
			final boolean watered) {
		if (watered && random.nextDouble() < .4) {
			tile.setTerrain(TileType.Plains);
		} else if (!tile.hasRiver() && random.nextDouble() < SIXTY_PERCENT) {
			tile.setTerrain(TileType.Plains);
		}
	}

	/**
	 * Add a suitable field or orchard to a tile.
	 *
	 * @param field if true, a field; if false, an orchard.
	 * @param tile the tile under consideration
	 * @param point the location of the tile under consideration
	 * @param main whether we should actually add the fixtures (i.e. is this the
	 *        main map)
	 * @param idFactory the factory to use to create ID numbers.
	 */
	private void addFieldOrOrchard(final boolean field, final Point point,
			final ITile tile, final boolean main, final IDFactory idFactory) {
		try {
			final int id = idFactory.createID(); // NOPMD
			if (field) {
				addFixture(
						tile,
						new Meadow(runner.recursiveConsultTable("grain", point,
								tile), true, true, id, FieldStatus.random(id)),
						main);
			} else {
				addFixture(
						tile,
						new Grove(true, true, runner.recursiveConsultTable(
								"fruit_trees", point, tile), id), main);
			}
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing encounter table", e);
		}
	}

	/**
	 * Add a forest.
	 *
	 * @param tile the tile under consideration
	 * @param point the location of the tile under consideration
	 * @param main whether we should actually add the fixtures (i.e. is this the
	 *        main map)
	 */
	private void addForest(final Point point, final ITile tile,
			final boolean main) {
		try {
			addFixture(
					tile,
					new Forest(runner.recursiveConsultTable(
							"temperate_major_tree", point, tile), false), main);
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing encounter table", e);
		}
	}

	/**
	 * Add a fixture to a tile if this is the main map.
	 *
	 * @param tile the tile to add the fixture to
	 * @param fix the fixture to add
	 * @param main whether this is the main map, i.e. should we actually add the
	 *        fixture
	 */
	private static void addFixture(final ITile tile, final TileFixture fix,
			final boolean main) {
		if (main) {
			tile.addFixture(fix);
		}
	}

	/**
	 * A tile's neighbors are its adjacent tiles. An "empty" tile (i.e. no
	 * fixtures, NotVisible---what is returned when a tile isn't in the map)
	 * shouldn't affect the caller at all; it should be as if it wasn't in the
	 * Iterable.
	 *
	 * @param point the location of the tile
	 * @return the locations of its neighbors.
	 */
	private static Iterable<Point> getNeighbors(final Point point) {
		final int row = point.row;
		final int col = point.col;
		final Iterable<Point> retval = Arrays.asList(PointFactory.point(row - 1, col - 1),
				PointFactory.point(row - 1, col),
				PointFactory.point(row - 1, col + 1),
				PointFactory.point(row, col - 1),
				PointFactory.point(row, col + 1),
				PointFactory.point(row + 1, col - 1),
				PointFactory.point(row + 1, col),
				PointFactory.point(row + 1, col + 1));
		assert retval != null;
		return retval;
	}

	/**
	 * @param point the tile's location
	 * @param map the map it's in
	 * @return whether the tile is adjacent to a town.
	 */
	private static boolean isAdjacentToTown(final Point point, final IMap map) {
		for (final Point npoint : getNeighbors(point)) {
			if (npoint == null) {
				continue;
			}
			final ITile neighbor = map.getTile(npoint);
			for (final TileFixture fix : neighbor) {
				if (fix instanceof Village || fix instanceof ITownFixture) {
					return true; // NOPMD
				}
			}
		}
		return false;
	}

	/**
	 * @param point the location of the tile
	 * @param map the map it's in
	 * @return whether the tile is adjacent to a river or ocean
	 */
	private static boolean isAdjacentToWater(final Point point, final IMap map) {
		for (final Point npoint : getNeighbors(point)) {
			if (npoint == null) {
				continue;
			}
			final ITile neighbor = map.getTile(npoint);
			if (!neighbor.hasRiver()
					|| TileType.Ocean.equals(neighbor.getTerrain())) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 * @return whether it already has a forest
	 */
	private static boolean hasForest(final ITile tile) {
		for (final TileFixture fix : tile) {
			if (fix instanceof Forest) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @return How many subtiles per tile the addRiver() algorithm is optimized
	 *         for.
	 */
	private static int optSubtilesPerTile() {
		return 4;
	}

	/**
	 * @param river a river
	 * @param tiles the subtiles to apply it to
	 */
	// ESCA-JAVA0076:
	private static void addRiver(final River river,
			final List<Pair<Point, ITile>> tiles) {
		if (RES_JUMP != optSubtilesPerTile()) {
			throw new IllegalStateException(
					"This function is tuned for 4 subtiles per tile per axis");
		}
		switch (river) {
		case East:
			tiles.get(10).second().addRiver(River.East);
			tiles.get(11).second().addRiver(River.East);
			tiles.get(11).second().addRiver(River.West);
			break;
		case Lake:
			tiles.get(10).second().addRiver(River.Lake);
			break;
		case North:
			tiles.get(2).second().addRiver(River.North);
			tiles.get(2).second().addRiver(River.South);
			tiles.get(6).second().addRiver(River.North);
			tiles.get(6).second().addRiver(River.South);
			tiles.get(10).second().addRiver(River.North);
			break;
		case South:
			tiles.get(10).second().addRiver(River.South);
			tiles.get(14).second().addRiver(River.South);
			tiles.get(14).second().addRiver(River.North);
			break;
		case West:
			tiles.get(8).second().addRiver(River.West);
			tiles.get(8).second().addRiver(River.East);
			tiles.get(9).second().addRiver(River.West);
			tiles.get(9).second().addRiver(River.East);
			tiles.get(10).second().addRiver(River.West);
			break;
		default:
			throw new IllegalStateException("Unknown River");
		}
	}

	/**
	 * @param point the location of the tile
	 * @return a seed for the RNG for conversion based on the given tile
	 */
	private static long getSeed(final Point point) {
		return (long) (point.col) << 32L + point.row;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "OneToTwoConverter";
	}
	/**
	 * @param args command-line arguments, main map first, then players' maps
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: OneToTwoConverter mainmap.xml [playermap.xml ...]");
			System.exit(1);
		} else {
			boolean first = true;
			final OneToTwoConverter converter = new OneToTwoConverter();
			final MapReaderAdapter reader = new MapReaderAdapter();
			for (final String arg : args) {
				if (arg == null) {
					continue;
				}
				// ESCA-JAVA0177:
				IMap old;
				try {
					old = reader.readMap(arg, Warning.INSTANCE);
				} catch (IOException | XMLStreamException
						| SPFormatException except) {
					printReadError(except, arg);
					if (first) {
						System.exit(2);
						break;
					} else {
						continue;
					}
				}
				final IMap newMap = converter.convert(old, first);
				try {
					reader.write(arg + ".converted.xml", newMap);
				} catch (IOException except) {
					System.err.print("I/O error writing to ");
					System.err.print(arg);
					System.err.println(".converted.xml");
					if (first) {
						System.exit(4);
					}
				}
				first = false;
			}
		}
	}
	/**
	 * Print a suitable error message.
	 * @param except the exception to handle
	 * @param filename the file being read
	 */
	private static void printReadError(final Exception except, final String filename) {
		if (except instanceof MapVersionException) {
			System.err.print("Unsupported map version while reading ");
			System.err.println(filename);
		} else if (except instanceof XMLStreamException) {
			System.err.println("Malformed XML in ");
			System.err.println(filename);
		} else if (except instanceof FileNotFoundException) {
			System.err.println("File ");
			System.err.print(filename);
			System.err.print(" not found");
		} else if (except instanceof IOException) {
			System.err.println("I/O error reading ");
			System.err.println(filename);
		} else if (except instanceof SPFormatException) {
			System.err.println("Bad SP XML in ");
			System.err.print(filename);
			System.err.print(" on line ");
			System.err.print(((SPFormatException) except).getLine());
			System.err.println(", as explained below:");
			System.err.println(except.getLocalizedMessage());
		} else {
			System.err.print("Unexpected error while reading ");
			System.err.print(filename);
			System.err.println(':');
			except.printStackTrace(System.err);
			System.exit(3);
		}
	}
}
