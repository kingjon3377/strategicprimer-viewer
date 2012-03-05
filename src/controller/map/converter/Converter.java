package controller.map.converter;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.ExplorationRunner;
import model.exploration.MissingTableException;
import model.map.Player;
import model.map.River;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.events.AbstractTownEvent;
import model.map.events.TownStatus;
import model.map.fixtures.Forest;
import model.map.fixtures.Ground;
import model.map.fixtures.Grove;
import model.map.fixtures.Hill;
import model.map.fixtures.Meadow;
import model.map.fixtures.Mountain;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.Sandbar;
import model.map.fixtures.Shrub;
import model.map.fixtures.TextFixture;
import model.map.fixtures.Village;
import controller.exploration.TableLoader;

/**
 * A class to convert a version-1 map to a version-2 map with greater
 * resolution.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class Converter {
	/**
	 * Constructor.
	 */
	public Converter() {
		new TableLoader().loadAllTables("tables", runner);
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
	private static final Logger LOGGER = Logger.getLogger(Converter.class
			.getName());
	/**
	 * The number of subtiles per tile on each axis.
	 */
	private static final int SUBTILES_PER_TILE = 4;
	/**
	 * The maximum number of iterations per tile.
	 */
	private static final int MAX_ITERATIONS = 100;

	/**
	 * @param old
	 *            a version-1 map
	 * @param main
	 *            whether the map is the main map (new encounter-type fixtures
	 *            don't go on players' maps)
	 * @return a version-2 equivalent with greater resolution
	 */
	public SPMap convert(final SPMap old, final boolean main) {
		final SPMap retval = new SPMap(2, old.rows() * SUBTILES_PER_TILE,
				old.cols() * SUBTILES_PER_TILE);
		for (Player player : old.getPlayers()) {
			retval.addPlayer(player);
		}
		final List<Tile> converted = new LinkedList<Tile>();
		for (int row = 0; row < old.rows(); row++) {
			for (int col = 0; col < old.cols(); col++) {
				for (Tile tile : convertTile(old.getTile(row, col), main)) {
					retval.addTile(tile);
					converted.add(tile);
				}
			}
		}
		final Random random = new Random(MAX_ITERATIONS);
		Collections.shuffle(converted, random);
		for (Tile tile : converted) {
			perturb(tile, retval, random, main);
		}
		return retval;
	}

	/**
	 * Create the initial list of subtiles for a tile.
	 * @param tile the tile on the old map
	 * @param main whether this is the main map or a player's map
	 * @return the equivalent higher-resolution tiles, in initial form
	 */
	private List<Tile> createInitialSubtiles(final Tile tile, final boolean main) {
		final List<Tile> initial = new LinkedList<Tile>();
		if (!tile.isEmpty()) {
			for (int i = 0; i < SUBTILES_PER_TILE; i++) {
			for (int j = 0; j < SUBTILES_PER_TILE; j++) {
				final int row = tile.getLocation().row() * SUBTILES_PER_TILE + i;
				final int col = tile.getLocation().col() * SUBTILES_PER_TILE + j;
				final Tile subtile = new Tile(row, col, tile.getType()); // NOPMD
				initial.add(subtile);
				convertSubtile(subtile, main);
			}
		}
		}
		return initial;
	}
	/**
	 * @param tile
	 *            a tile on the old map
	 * @param main whether this is the main  map or a player's map
	 * @return the equivalent higher-resolution tiles.
	 */
	private List<Tile> convertTile(final Tile tile, final boolean main) {
		final List<Tile> initial = createInitialSubtiles(tile, main);
		if (!tile.isEmpty()) {
		tile.addFixture(new Village(TownStatus.Active));
		final List<TileFixture> fixtures = new LinkedList<TileFixture>(
				tile.getContents());
		if (tile.hasRiver()) {
			final RiverFixture rivers = tile.getRivers();
			for (River river : rivers) {
				addRiver(river, initial);
			}
			fixtures.remove(rivers);
		}
		final Random random = new Random(getSeed(tile));
		Collections.shuffle(initial, random);
		Collections.shuffle(fixtures, random);
		int iterations = 0;
		while (iterations < MAX_ITERATIONS && !fixtures.isEmpty()) {
			if (isSubtileSuitable(initial.get(0))) {
				changeFor(initial.get(0), fixtures.get(0));
				initial.get(0).addFixture(fixtures.remove(0));
			}
			initial.add(initial.remove(0));
			iterations++;
		}
		if (iterations == MAX_ITERATIONS) {
			LOGGER.severe("Maximum number of iterations reached on tile ("
					+ tile.getLocation().row() + ", " + tile.getLocation().col() + "); forcing ...");
			while (!fixtures.isEmpty()) {
				final Tile subtile = initial.get(0);
				subtile.addFixture(fixtures.remove(0));
				subtile.addFixture(new TextFixture(// NOPMD
						"FIXME: A fixture here was force-added after MAX_ITER",
						NEXT_TURN));
				initial.add(initial.remove(0));
			}
		}
		}
		return initial;
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
	 * @param tile
	 *            the tile to convert
	 * @param main whether this is the main map or a player's map
	 */
	@SuppressWarnings("deprecation")
	private void convertSubtile(final Tile tile, final boolean main) {
		try {
			if (TileType.Mountain.equals(tile.getType())) {
				tile.setType(TileType.Plains);
				tile.addFixture(new Mountain());
			} else if (TileType.TemperateForest.equals(tile.getType())) {
				if (!hasForest(tile)) {
					tile.addFixture(new Forest(runner.getPrimaryTree(tile),
							false));
				}
				tile.setType(TileType.Plains);
			} else if (TileType.BorealForest.equals(tile.getType())) {
				if (!hasForest(tile)) {
					tile.addFixture(new Forest(runner.getPrimaryTree(tile), false));
				}
				tile.setType(TileType.Steppe);
			}
			addFixture(tile, new Ground(runner.getPrimaryRock(tile), false), main);
		} catch (MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing table", e);
		}
	}

	/**
	 * Determine whether a subtile is suitable for more fixtures. It's suitable
	 * if its only fixtures are Forests, Mountains, Ground or other similar
	 * "background".
	 * 
	 * @param tile
	 *            the tile
	 * @return whether it's suitable
	 */
	private static boolean isSubtileSuitable(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
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
	 * @param tile
	 *            the tile to prepare
	 * @param fix
	 *            the fixture to prepare it for
	 */
	private static void changeFor(final Tile tile, final TileFixture fix) {
		if (fix instanceof Village || fix instanceof AbstractTownEvent) {
			final Set<TileFixture> fixtures = new HashSet<TileFixture>(
					tile.getContents());
			for (TileFixture fixture : fixtures) {
				if (fixture instanceof Forest) {
					tile.removeFixture(fixture);
				}
			}
		}
	}

	/**
	 * Possibly make a random change to a tile.
	 * 
	 * @param tile
	 *            the tile under consideration
	 * @param map
	 *            the map it's on, so we can consider adjacent tiles
	 * @param random
	 *            the source of randomness (so this is repeatable with players'
	 *            maps)
	 * @param main
	 *            whether we should actually add the fixtures (i.e. is this the
	 *            main map)
	 */
	private void perturb(final Tile tile, final SPMap map, final Random random,
			final boolean main) {
		try {
			if (!TileType.Ocean.equals(tile.getType())) {
				if (isAdjacentToTown(tile, map)
						&& random.nextDouble() < SIXTY_PERCENT) {
					if (random.nextBoolean()) {
						addFixture(tile,
								new Meadow(runner.recursiveConsultTable("grain", tile),
										true, true), main);
					} else {
						addFixture(
								tile,
								new Grove(true, false, runner.recursiveConsultTable(
										"fruit_trees", tile)), main);
					}
				} else if (TileType.Desert.equals(tile.getType())) {
					if (isAdjacentToWater(tile, map) && random.nextDouble() < .4) {
						tile.setType(TileType.Plains);
					} else if (!tile.hasRiver() && random.nextDouble() < SIXTY_PERCENT) {
						tile.setType(TileType.Plains);
					}
				} else if (random.nextDouble() < .1) {
					addFixture(
							tile,
							new Forest(runner.recursiveConsultTable(
									"temperate_major_tree", tile), false), main);
				}
			}
		} catch (MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing encounter table", e);
		}
	}

	/**
	 * Add a fixture to a tile if this is the main map.
	 * 
	 * @param tile
	 *            the tile to add the fixture to
	 * @param fix
	 *            the fixture to add
	 * @param main
	 *            whether this is the main map, i.e. should we actually add the
	 *            fixture
	 */
	private static void addFixture(final Tile tile, final TileFixture fix,
			final boolean main) {
		if (main) {
			tile.addFixture(fix);
		}
	}

	/**
	 * @param tile
	 *            a tile
	 * @param map
	 *            the map it's in
	 * @return whether the tile is adjacent to a town.
	 */
	private static boolean isAdjacentToTown(final Tile tile, final SPMap map) {
		for (int row = tile.getLocation().row() - 1; row < tile.getLocation().row() + 2; row++) {
			for (int col = tile.getLocation().col() - 1; col < tile.getLocation().col() + 2; col++) {
				final Tile neighbor = map.getTile(row, col);
				if (neighbor.equals(tile)) {
					continue;
				}
				for (final TileFixture fix : neighbor.getContents()) {
					if (fix instanceof Village
							|| fix instanceof AbstractTownEvent) {
						return true; // NOPMD
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param tile
	 *            a tile
	 * @param map
	 *            the map it's in
	 * @return whether the tile is adjacent to a river or ocean
	 */
	private static boolean isAdjacentToWater(final Tile tile, final SPMap map) {
		for (int row = tile.getLocation().row() - 1; row < tile.getLocation().row() + 2; row++) {
			for (int col = tile.getLocation().col() - 1; col < tile.getLocation().col() + 2; col++) {
				final Tile neighbor = map.getTile(row, col);
				if (neighbor.equals(tile)) {
					continue;
				}
				if (!neighbor.hasRiver()
						|| TileType.Ocean.equals(neighbor.getType())) {
					return true; // NOPMD
				}
			}
		}
		return false;
	}
	/**
	 * @param tile a tile
	 * @return whether it already has a forest
	 */
	private static boolean hasForest(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof Forest) {
				return true; // NOPMD
			}
		}
		return false;
	}
	/**
	 * @param river a river
	 * @param tiles the subtiles to apply it to
	 */
	// ESCA-JAVA0076:
	@SuppressWarnings("unused")
	private static void addRiver(final River river, final List<Tile> tiles) {
		if (SUBTILES_PER_TILE != 4) {
			throw new IllegalStateException("This function is tuned for 4 subtiles per tile per axis");
		}
		switch (river) {
		case East:
			tiles.get(10).addRiver(River.East);
			tiles.get(11).addRiver(River.East);
			tiles.get(11).addRiver(River.West);
			break;
		case Lake:
			tiles.get(10).addRiver(River.Lake);
			break;
		case North:
			tiles.get(2).addRiver(River.North);
			tiles.get(2).addRiver(River.South);
			tiles.get(6).addRiver(River.North);
			tiles.get(6).addRiver(River.South);
			tiles.get(10).addRiver(River.North);
			break;
		case South:
			tiles.get(10).addRiver(River.South);
			tiles.get(14).addRiver(River.South);
			tiles.get(14).addRiver(River.North);
			break;
		case West:
			tiles.get(8).addRiver(River.West);
			tiles.get(8).addRiver(River.East);
			tiles.get(9).addRiver(River.West);
			tiles.get(9).addRiver(River.East);
			tiles.get(10).addRiver(River.West);
			break;
		default:
			throw new IllegalStateException("Unknown River");
		}
	}
	/**
	 * @param tile a tile
	 * @return a seed for the RNG for conversion based on the given tile 
	 */
	private static long getSeed(final Tile tile) {
		return (long) (tile.getLocation().col()) << 32L + tile.getLocation().row();
	}
}
