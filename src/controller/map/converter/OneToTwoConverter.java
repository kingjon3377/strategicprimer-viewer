package controller.map.converter; // NOPMD

import controller.exploration.TableLoader;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.xml.stream.XMLStreamException;
import model.exploration.old.ExplorationRunner;
import model.exploration.old.MissingTableException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
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
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;
import util.Warning;

import static util.NullCleaner.assertNotNull;

/**
 * A class to convert a version-1 map to a version-2 map with greater resolution.
 *
 * TODO: Write tests.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class OneToTwoConverter { // NOPMD
	/**
	 * Sixty percent. Our probability for a couple of perturbations.
	 */
	private static final double SIXTY_PERCENT = 0.6;
	/**
	 * The next turn. Use for TextFixtures to replace with generated encounters later.
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
	 * An exploration runner, to get forest and ground types from.
	 */
	private final ExplorationRunner runner = new ExplorationRunner();

	/**
	 * The probability of turning a watered desert to plains.
	 */
	private static final double DESERT_TO_PLAINS = 0.4;
	/**
	 * The probability of adding a forest to a tile.
	 */
	private static final double ADD_FOREST_PROB = 0.1;

	/**
	 * Constructor.
	 */
	public OneToTwoConverter() {
		TableLoader.loadAllTables("tables", runner);
	}

	/**
	 * @param old  a version-1 map
	 * @param main whether the map is the main map (new encounter-type fixtures don't go
	 *             on players' maps)
	 * @return a version-2 equivalent with greater resolution
	 */
	public IMapNG convert(final IMapNG old, final boolean main) {
		final MapDimensions oldDim = old.dimensions();
		final IMutableMapNG retval =
				new SPMapNG(new MapDimensions(oldDim.rows * RES_JUMP,
													 oldDim.cols * RES_JUMP, 2),
								   new PlayerCollection(), -1);
		Player independent = new Player(-1, "independent");
		for (final Player player : old.players()) {
			retval.addPlayer(player);
			if (player.isIndependent()) {
				independent = player;
			}
		}
		final List<Point> converted = new LinkedList<>();
		final IDFactory idFactory = new IDFactory();
		for (int row = 0; row < oldDim.rows; row++) {
			for (int col = 0; col < oldDim.cols; col++) {
				converted.addAll(convertTile(PointFactory.point(row, col), old,
						retval, main, idFactory, independent));
			}
		}
		final Random random = new Random(MAX_ITERATIONS);
		Collections.shuffle(converted, random);
		for (final Point point : converted) {
			perturb(point, retval, random, main, idFactory);
		}
		return retval;
	}

	/**
	 * @param map   a map
	 * @param point a point
	 * @return whether that location in the map is empty (no terrain type, no ground, no
	 * forest, no rivers, no fixtures)
	 */
	private static boolean isPointEmpty(final IMapNG map, final Point point) {
		return (TileType.NotVisible == map.getBaseTerrain(point))
					   && (map.getGround(point) == null) && (map.getForest(point) ==
																	 null)
					   && !map.getRivers(point).iterator().hasNext()
					   && !map.getOtherFixtures(point).iterator().hasNext();
	}

	/**
	 * Create the initial list of subtiles for a tile.
	 *
	 * @param point  the location in the old map
	 * @param oldMap the old map
	 * @param newMap the new map
	 * @param main   whether this is the main map or a player's map
	 * @return the equivalent higher-resolution points
	 */
	private List<Point> createInitialSubtiles(final Point point,
											  final IMapNG oldMap,
											  final IMutableMapNG newMap,
											  final boolean main) {
		final List<Point> initial = new LinkedList<>();
		if (!isPointEmpty(oldMap, point)) {
			for (int i = 0; i < RES_JUMP; i++) {
				for (int j = 0; j < RES_JUMP; j++) {
					final int row = (point.row * RES_JUMP) + i;
					final int col = (point.col * RES_JUMP) + j;
					final Point subpoint = PointFactory.point(row, col);
					newMap.setBaseTerrain(subpoint, oldMap.getBaseTerrain(point));
					initial.add(subpoint);
					convertSubtile(subpoint, newMap, main);
				}
			}
		}
		return initial;
	}

	/**
	 * @param point             a location in the old map
	 * @param oldMap            the old map
	 * @param newMap            the new map
	 * @param main              whether this is the main map or a player's map
	 * @param idFactory         the IDFactory to use to get IDs.
	 * @param independentPlayer the Player to own villages
	 * @return a list of the points we affected in this pass
	 */
	private Collection<Point> convertTile(final Point point,
										  final IMapNG oldMap, final IMutableMapNG
																	   newMap,
										  final boolean main,
										  final IDFactory idFactory,
										  final Player independentPlayer) {
		final List<Point> initial = createInitialSubtiles(point,
				oldMap, newMap, main);
		if (!isPointEmpty(oldMap, point)) {
			final int idNum = idFactory.createID();
			if (oldMap instanceof IMutableMapNG) {
				((IMutableMapNG) oldMap).addFixture(point, new Village(
																			  TownStatus
																					  .Active,
																			  "", idNum,
																			  independentPlayer,
																			  RaceFactory
																					  .getRace(
																							  new Random(idNum))));
			}
			final List<TileFixture> fixtures = new LinkedList<>();
			@Nullable
			final Ground ground = oldMap.getGround(point);
			if (ground != null) {
				fixtures.add(ground);
			}
			@Nullable
			final Forest forest = oldMap.getForest(point);
			if (forest != null) {
				fixtures.add(forest);
			}
			oldMap.getOtherFixtures(point).forEach(fixtures::add);
			separateRivers(point, initial, oldMap, newMap);
			final Random random = new Random(getSeed(point));
			Collections.shuffle(initial, random);
			Collections.shuffle(fixtures, random);
			int iterations;
			for (iterations = 0; (iterations < MAX_ITERATIONS)
										 && !fixtures.isEmpty(); iterations++) {
				if (isSubtileSuitable(newMap, assertNotNull(initial.get(0)))) {
					final TileFixture fix = assertNotNull(fixtures.remove(0));
					changeFor(newMap, assertNotNull(initial.get(0)), fix);
					addFixture(newMap, assertNotNull(initial.get(0)), fix, main);
				}
				initial.add(initial.remove(0));
			}
			if (iterations == MAX_ITERATIONS) {
				LOGGER.severe("Maximum number of iterations reached on tile ("
									  + point.row + ", " + point.col + "); forcing ...");
				while (!fixtures.isEmpty()) {
					final Point subtile = assertNotNull(initial.get(0));
					newMap.addFixture(subtile,
							assertNotNull(fixtures.remove(0)));
					newMap.addFixture(subtile, new TextFixture(//NOPMD
																	  "FIXME: A fixture " +
																			  "here was " +
																			  "force-added after MAX_ITER",
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
	 * @param point   the location being handled
	 * @param initial the initial set of subpoints
	 * @param oldMap  the old map
	 * @param newMap  the new map
	 */
	private static void separateRivers(final Point point,
									   final List<Point> initial, final IMapNG oldMap,
									   final IMutableMapNG newMap) {
		for (final River river : oldMap.getRivers(point)) {
			addRiver(river, initial, newMap);
		}
	}

	/**
	 * Convert a tile. That is, change it from a forest or mountain type to the proper
	 * replacement type plus the proper fixture. Also, in any case, add the proper
	 * Ground.
	 *
	 * @param map   the map
	 * @param point the location to convert
	 * @param main  whether this is the main map or a player's map
	 */
	@SuppressWarnings("deprecation")
	private void convertSubtile(final Point point, final IMutableMapNG map,
								final boolean main) {
		try {
			if (TileType.Mountain == map.getBaseTerrain(point)) {
				map.setBaseTerrain(point, TileType.Plains);
				map.setMountainous(point, true);
			} else if (TileType.TemperateForest == map.getBaseTerrain(point)) {
				if (!hasForest(map, point)) {
					map.setForest(
							point,
							new Forest(runner.getPrimaryTree(point,
									map.getBaseTerrain(point),
									map.getOtherFixtures(point)), false));
				}
				map.setBaseTerrain(point, TileType.Plains);
			} else if (TileType.BorealForest == map.getBaseTerrain(point)) {
				if (!hasForest(map, point)) {
					map.setForest(
							point,
							new Forest(runner.getPrimaryTree(point,
									map.getBaseTerrain(point),
									map.getOtherFixtures(point)), false));
				}
				map.setBaseTerrain(point, TileType.Steppe);
			}
			addFixture(
					map,
					point,
					new Ground(runner.getPrimaryRock(point,
							map.getBaseTerrain(point),
							map.getOtherFixtures(point)), false), main);
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing table", e);
		}
	}

	/**
	 * Determine whether a subtile is suitable for more fixtures. It's suitable if its
	 * only fixtures are Forests, Mountains, Ground or other similar "background".
	 *
	 * @param point the location in the map
	 * @param map   the map
	 * @return whether that location is suitable
	 */
	private static boolean isSubtileSuitable(final IMapNG map, final Point point) {
		return StreamSupport.stream(map.getOtherFixtures(point).spliterator(), false)
					   .anyMatch(OneToTwoConverter::isBackground);
	}

	/**
	 * @param fix a fixture
	 * @return true if it's "background", not making a subtile unsuitable for more
	 * fixtures, false otherwise.
	 */
	private static boolean isBackground(final TileFixture fix) {
		return (fix instanceof Forest) || (fix instanceof Mountain)
					   || (fix instanceof Ground) || (fix instanceof Sandbar)
					   || (fix instanceof Shrub) || (fix instanceof Meadow)
					   || (fix instanceof Hill);
	}

	/**
	 * Prepare a subtile for a specified new fixture. At present, the only change this
	 * involves is removing any forests if there's a village or TownEvent.
	 *
	 * @param map   the map to prepare
	 * @param point the location to prepare
	 * @param fix   the fixture to prepare it for
	 */
	private static void changeFor(final IMutableMapNG map, final Point point,
								  final TileFixture fix) {
		if ((fix instanceof Village) || (fix instanceof ITownFixture)) {
			final Collection<TileFixture> forests =
					StreamSupport.stream(map.getOtherFixtures(point).spliterator(),
							false)
							.filter(p -> p instanceof Forest).collect(

							Collectors.toList());
			for (final TileFixture fixture : forests) {
				map.removeFixture(point, fixture);
			}
			map.setForest(point, null);
		}
	}

	/**
	 * Possibly make a random change to a tile.
	 *
	 * @param point  its location
	 * @param map    the map it's on, so we can consider adjacent tiles
	 * @param random the source of randomness (so this is repeatable with players' maps)
	 * @param main   whether we should actually add the fixtures (i.e. is this the main
	 *               map)
	 * @param idFac  the factory to use to create ID numbers
	 */
	private void perturb(final Point point, final IMutableMapNG map,
						 final Random random, final boolean main, final IDFactory
																		  idFac) {
		if (TileType.Ocean != map.getBaseTerrain(point)) {
			if (isAdjacentToTown(point, map)
						&& (random.nextDouble() < SIXTY_PERCENT)) {
				addFieldOrOrchard(random.nextBoolean(), point, map, main,
						idFac);
			} else if (TileType.Desert == map.getBaseTerrain(point)) {
				final boolean watered = hasAdjacentWater(point, map);
				waterDesert(map, point, random, watered);
			} else if (random.nextDouble() < ADD_FOREST_PROB) {
				addForest(point, map, main);
			}
		}
	}

	/**
	 * Make changes to a desert tile based on water.
	 *
	 * @param map     the map
	 * @param point   the location being considered
	 * @param random  the source of randomness
	 * @param watered whether the tile is adjacent to water
	 */
	private static void waterDesert(final IMutableMapNG map, final Point point,
									final Random random, final boolean watered) {
		if (watered && (random.nextDouble() < DESERT_TO_PLAINS)) {
			map.setBaseTerrain(point, TileType.Plains);
		} else if (!map.getRivers(point).iterator().hasNext()
						   &&
						   (random.nextDouble() <
									SIXTY_PERCENT)) {
			map.setBaseTerrain(point, TileType.Plains);
		}
	}

	/**
	 * Add a suitable field or orchard to a tile.
	 *
	 * @param field     if true, a field; if false, an orchard.
	 * @param map       the map
	 * @param point     the location of the tile under consideration
	 * @param main      whether we should actually add the fixtures (i.e. is this the
	 *                     main
	 *                  map)
	 * @param idFactory the factory to use to create ID numbers.
	 */
	private void addFieldOrOrchard(final boolean field, final Point point,
								   final IMutableMapNG map, final boolean main,
								   final IDFactory idFactory) {
		try {
			final int id = idFactory.createID(); // NOPMD
			if (field) {
				addFixture(
						map,
						point,
						new Meadow(runner.recursiveConsultTable("grain", point,
								map.getBaseTerrain(point),
								map.getOtherFixtures(point)), true, true, id,
										  FieldStatus.random(id)), main);
			} else {
				addFixture(
						map,
						point,
						new Grove(true, true, runner.recursiveConsultTable(
								"fruit_trees", point,
								map.getBaseTerrain(point),
								map.getOtherFixtures(point)), id), main);
			}
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing encounter table", e);
		}
	}

	/**
	 * Add a forest.
	 *
	 * @param map   the map
	 * @param point the location under consideration
	 * @param main  whether we should actually add the fixtures (i.e. is this the main
	 *              map)
	 */
	private void addForest(final Point point, final IMutableMapNG map,
						   final boolean main) {
		try {
			addFixture(
					map,
					point,
					new Forest(runner.recursiveConsultTable(
							"temperate_major_tree", point,
							map.getBaseTerrain(point),
							map.getOtherFixtures(point)), false), main);
		} catch (final MissingTableException e) {
			LOGGER.log(Level.WARNING, "Missing encounter table", e);
		}
	}

	/**
	 * Add a fixture to a tile if this is the main map.
	 *
	 * @param map   the map to add the fixture to
	 * @param point the location to add the fixture to
	 * @param fix   the fixture to add
	 * @param main  whether this is the main map, i.e. should we actually add the fixture
	 */
	private static void addFixture(final IMutableMapNG map, final Point point,
								   final TileFixture fix, final boolean main) {
		if (main) {
			if ((fix instanceof Ground) && (map.getGround(point) == null)) {
				map.setGround(point, (Ground) fix);
			} else if ((fix instanceof Forest) && (map.getForest(point) == null)) {
				map.setForest(point, (Forest) fix);
			} else {
				map.addFixture(point, fix);
			}
		}
	}

	/**
	 * A tile's neighbors are its adjacent tiles. An "empty" tile (i.e. no fixtures,
	 * NotVisible---what is returned when a tile isn't in the map) shouldn't affect the
	 * caller at all; it should be as if it wasn't in the Iterable.
	 *
	 * @param point the location of the tile
	 * @return the locations of its neighbors.
	 */
	private static Iterable<Point> getNeighbors(final Point point) {
		final int row = point.row;
		final int col = point.col;
		return assertNotNull(Arrays.asList(
				PointFactory.point(row - 1, col - 1),
				PointFactory.point(row - 1, col),
				PointFactory.point(row - 1, col + 1),
				PointFactory.point(row, col - 1),
				PointFactory.point(row, col + 1),
				PointFactory.point(row + 1, col - 1),
				PointFactory.point(row + 1, col),
				PointFactory.point(row + 1, col + 1)));
	}

	/**
	 * @param point the tile's location
	 * @param map   the map it's in
	 * @return whether the tile is adjacent to a town.
	 */
	private static boolean isAdjacentToTown(final Point point, final IMapNG map) {
		return StreamSupport.stream(getNeighbors(point).spliterator(), false)
					   .flatMap(npoint -> StreamSupport.stream(map.getOtherFixtures
																		   (npoint)
																	   .spliterator(),
							   false))
					   .anyMatch(fix -> fix instanceof ITownFixture);
	}

	/**
	 * @param point the location of the tile
	 * @param map   the map it's in
	 * @return whether the tile is adjacent to a river or ocean
	 */
	private static boolean hasAdjacentWater(final Point point, final IMapNG map) {
		return StreamSupport.stream(getNeighbors(point).spliterator(), false).anyMatch(
				npoint -> map.getRivers(npoint).iterator().hasNext() ||
								  (TileType.Ocean == map.getBaseTerrain(npoint)));
	}

	/**
	 * @param point a location
	 * @param map   the map
	 * @return whether that location already has a forest
	 */
	private static boolean hasForest(final IMapNG map, final Point point) {
		return (map.getForest(point) != null) ||
					   StreamSupport.stream(map.getOtherFixtures(point).spliterator(),
							   false)
							   .anyMatch(fix -> fix instanceof Forest);
	}

	/**
	 * @return How many subtiles per tile the addRiver() algorithm is optimized for.
	 */
	private static int optSubtilesPerTile() {
		return 4;
	}

	/**
	 * @param river  a river
	 * @param points the subpoints to apply it to
	 * @param map    the map to work in
	 */
	private static void addRiver(final River river,
								 final List<Point> points, final IMutableMapNG map) {
		if (RES_JUMP != optSubtilesPerTile()) {
			throw new IllegalStateException(
												   "This function is tuned for 4 " +
														   "subtiles per tile per axis");
		}
		switch (river) {
		case East:
			map.addRivers(assertNotNull(points.get(10)), River.East);
			map.addRivers(assertNotNull(points.get(11)), River.East);
			map.addRivers(assertNotNull(points.get(11)), River.West);
			break;
		case Lake:
			map.addRivers(assertNotNull(points.get(10)), River.Lake);
			break;
		case North:
			map.addRivers(assertNotNull(points.get(2)), River.North);
			map.addRivers(assertNotNull(points.get(2)), River.South);
			map.addRivers(assertNotNull(points.get(6)), River.North);
			map.addRivers(assertNotNull(points.get(6)), River.South);
			map.addRivers(assertNotNull(points.get(10)), River.North);
			break;
		case South:
			map.addRivers(assertNotNull(points.get(10)), River.South);
			map.addRivers(assertNotNull(points.get(14)), River.South);
			map.addRivers(assertNotNull(points.get(14)), River.North);
			break;
		case West:
			map.addRivers(assertNotNull(points.get(8)), River.West);
			map.addRivers(assertNotNull(points.get(8)), River.East);
			map.addRivers(assertNotNull(points.get(9)), River.West);
			map.addRivers(assertNotNull(points.get(9)), River.East);
			map.addRivers(assertNotNull(points.get(10)), River.West);
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
		return (long) point.col << (32L + point.row);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "OneToTwoConverter";
	}

	/**
	 * @param args command-line arguments, main map first, then players' maps
	 */
	public static void main(final String... args) {
		if (args.length == 0) {
			System.err.print("Usage: ");
			System.err.print(OneToTwoConverter.class.getSimpleName());
			System.err.println(" mainmap.xml [playermap.xml ...]");
			System.exit(1);
		} else {
			boolean first = true;
			final OneToTwoConverter converter = new OneToTwoConverter();
			final MapReaderAdapter reader = new MapReaderAdapter();
			for (final String arg : args) {
				if (arg == null) {
					continue;
				}
				final File file = new File(arg);
				final IMapNG old;
				try {
					old = reader.readMap(file, Warning.INSTANCE);
				} catch (final IOException | XMLStreamException
									   | SPFormatException except) {
					printReadError(except, arg);
					if (first) {
						System.exit(2);
						break;
					} else {
						continue;
					}
				}
				final IMapNG newMap = converter.convert(old, first);
				try {
					reader.write(new File(arg + ".converted.xml"), newMap);
				} catch (final IOException except) {
					LOGGER.log(Level.SEVERE,
							"I/O error writing to " + arg + ".converted.xml", except);
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
	 *
	 * @param except   the exception to handle
	 * @param filename the file being read
	 */
	private static void printReadError(final Exception except,
									   final String filename) {
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
