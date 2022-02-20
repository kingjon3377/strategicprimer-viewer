package drivers.exploration.old;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import lovelace.util.ThrowingConsumer;
import java.util.Arrays;
import java.util.Iterator;

import lovelace.util.FileContentsReader;

import common.map.Point;
import common.map.TileType;
import common.map.TileFixture;
import common.map.MapDimensions;

import common.map.fixtures.terrain.Forest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.NoSuchFileException;
import java.nio.charset.StandardCharsets;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.StreamSupport;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * A class to create exploration results. The initial implementation is a bit
 * hackish, and should be generalized and improved---except that the entire
 * idea of generating results from "encounter tables" instead of selecting from
 * "fixtures" stored in the map has been abandoned, after using it to generate
 * the tile-fixtures in each tile in the main map.  This old method is now only
 * used in the town-contents generator and in the TODO-fixing driver's handling
 * of broken town production, consumption, and skill levels.
 */
public final class ExplorationRunner {
	private static final Logger LOGGER = Logger.getLogger(ExplorationRunner.class.getName());

	private final Map<String, EncounterTable> tables = new HashMap<>();

	/**
	 * Get a table by name.
	 *
	 * @throws MissingTableException if there is no table by that name
	 */
	// Used by the table debugger.
	/* package */ EncounterTable getTable(final String name) throws MissingTableException {
		return Optional.ofNullable(tables.get(name))
			.orElseThrow(() -> new MissingTableException(name));
	}

	/**
	 * Whether we have a table of the given name.
	 */
	public boolean hasTable(final String name) {
		return tables.containsKey(name);
	}

	/**
	 * Split a string on hash-marks.
	 */
	private static List<String> splitOnHash(final String string) {
		return Arrays.asList(string.split("#", 3));
	}

	/**
	 * Consult a table, and if a result indicates recursion, perform it.
	 * Recursion is indicated by hash-marks ({@code #}) around the
	 * name of the table to call; results are undefined if there are more
	 * than two hash marks in any given String, or if either is at the
	 * beginning or end of the string, since we use {@link String#split}.
	 */
	public String recursiveConsultTable(final String table, final Point location, final @Nullable TileType terrain,
	                                    final boolean mountainous, final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions)
			throws MissingTableException {
		final String result = consultTable(table, location, terrain, mountainous, fixtures, mapDimensions);
		if (result.contains("#")) {
			final List<String> broken = new LinkedList<>(splitOnHash(result));
			if (broken.size() < 2) {
				throw new IllegalStateException(String.format("Unexpected result of split: '%s' -> %s", result, broken));
			}
			final String before = broken.remove(0);
			final String middle = broken.remove(0);
			final StringBuilder builder = new StringBuilder();
			builder.append(before);
			builder.append(recursiveConsultTable(middle, location, terrain, mountainous,
				fixtures, mapDimensions));
			broken.forEach(builder::append);
			return builder.toString();
		} else {
			return result;
		}
	}

	/**
	 * Check whether a table contains recursive calls to a table that
	 * doesn't exist.
	 */
	public boolean recursiveCheck(final String table) {
		return recursiveCheck(table, new HashSet<>());
	}

	/**
	 * Check whether a table contains recursive calls to a table that
	 * doesn't exist.
	 */
	private boolean recursiveCheck(final String table, final Set<String> state) {
		if (state.contains(table)) {
			return false;
		}
		state.add(table);
		if (tables.containsKey(table)) {
			try {
				for (final String string : getTable(table).getAllEvents()) {
					if (string.contains("#") && recursiveCheck(
							splitOnHash(string).get(1), state)) {
						return true;
					}
				}
			} catch (final MissingTableException except) {
				LOGGER.log(Level.INFO, "Missing table " + table, except);
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check whether any table contains recursive calls to a table that doesn't exist.
	 */
	public boolean globalRecursiveCheck() {
		final Set<String> state = new HashSet<>();
		// TODO: Use concurrent set instead of sequential()
		return tables.keySet().stream().sequential().anyMatch(table -> recursiveCheck(table, state));
	}

	/**
	 * Print the names of any tables that are called but don't exist yet.
	 */
	public void verboseRecursiveCheck(final String table, final ThrowingConsumer<String, IOException> ostream) throws IOException {
		verboseRecursiveCheck(table, ostream, new HashSet<>());
	}

	/**
	 * Print the names of any tables that are called but don't exist yet.
	 */
	public void verboseRecursiveCheck(final String table, final ThrowingConsumer<String, IOException> ostream, final Set<String> state)
			throws IOException {
		if (!state.contains(table)) {
			state.add(table);
			if (tables.containsKey(table)) {
				try {
					for (final String string : getTable(table).getAllEvents()) {
						if (string.contains("#")) {
							verboseRecursiveCheck(
								splitOnHash(string).get(1), ostream, state);
						}
					}
				} catch (final MissingTableException except) {
					ostream.accept(except.getTable());
				}
			} else {
				ostream.accept(table);
			}
		}
	}

	/**
	 * Print the names of any tables that are called but don't exist yet.
	 */
	public void verboseGlobalRecursiveCheck(final ThrowingConsumer<String, IOException> ostream) throws IOException {
		final Set<String> state = new HashSet<>();
		for (final String table : tables.keySet()) {
			verboseRecursiveCheck(table, ostream, state);
		}
	}

	/**
	 * Consult a table. (Look up the given tile if it's a quadrant table,
	 * roll on it if it's a random-encounter table.) Note that the result
	 * may be or include the name of another table, which should then be
	 * consultd.
	 *
	 * @param table The name of the table to consult
	 * @param location The location of the tile
	 * @param terrain The terrain there. Null if unknown.
	 * @param mountainous Whether the tile is mountainous.
	 * @param fixtures Any fixtures there
	 * @param mapDimensions The dimensions of the map
	 */
	public String consultTable(final String table, final Point location, final @Nullable TileType terrain,
	                           final boolean mountainous, final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions)
			throws MissingTableException {
		return getTable(table).generateEvent(location, terrain, mountainous, fixtures,
			mapDimensions);
	}

	/**
	 * Get the primary rock at the given location.
	 *
	 * @param location The location of the tile
	 * @param terrain The terrain there.
	 * @param mountainous Whether the tile is mountainous.
	 * @param fixtures Any fixtures there
	 * @param mapDimensions The dimensions of the map
	 */
	public String getPrimaryRock(final Point location, final TileType terrain, final boolean mountainous,
	                             final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions)
			throws MissingTableException {
		return consultTable("major_rock", location, terrain, mountainous, fixtures, mapDimensions);
	}

	/**
	 * Get the primary forest at the given location.
	 *
	 * @param location The location of the tile
	 * @param terrain The terrain there.
	 * @param mountainous Whether the tile is mountainous.
	 * @param fixtures Any fixtures there
	 * @param mapDimensions The dimensions of the map
	 */
	public String getPrimaryTree(final Point location, final TileType terrain, final boolean mountainous,
	                             final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions)
			throws MissingTableException {
		switch (terrain) {
		case Steppe:
			if (StreamSupport.stream(fixtures.spliterator(), true)
					.anyMatch(Forest.class::isInstance)) {
				return consultTable("boreal_major_tree", location, terrain,
					mountainous, fixtures, mapDimensions);
			}
			break;
		case Plains:
			if (StreamSupport.stream(fixtures.spliterator(), true)
					.anyMatch(Forest.class::isInstance)) {
				return consultTable("temperate_major_tree", location, terrain,
					mountainous, fixtures, mapDimensions);
			}
			break;
		}
		throw new IllegalArgumentException("Only forests have primary trees");
	}

	/**
	 * Get the "default results" (primary rock and primary forest) for the given location.
	 *
	 * @param location The location of the tile
	 * @param terrain The terrain there.
	 * @param mountainous Whether the tile is mountainous.
	 * @param fixtures Any fixtures there
	 * @param mapDimensions The dimensions of the map
	 */
	public String defaultResults(final Point location, final TileType terrain, final boolean mountainous,
	                             final Iterable<TileFixture> fixtures, final MapDimensions mapDimensions)
			throws MissingTableException {
		if (StreamSupport.stream(fixtures.spliterator(), true).anyMatch(Forest.class::isInstance)
				&& (terrain == TileType.Steppe || terrain == TileType.Plains)) {
			return String.format(
				"The primary rock type here is %s.%nThe main kind of tree here is %s.%n",
				getPrimaryRock(location, terrain, mountainous, fixtures, mapDimensions),
				getPrimaryTree(location, terrain, mountainous, fixtures, mapDimensions));
		} else {
			return String.format("The primary rock type here is %s.",
				getPrimaryRock(location, terrain, mountainous, fixtures, mapDimensions));
		}
	}

	/**
	 * Add a table.
	 */
	/* package */ void loadTable(final String name, final EncounterTable table) {
		tables.put(name, table);
	}

	/**
	 * Load a table from a data stream into the runner.
	 */
	/* package */ void loadTableFromDataStream(final Iterator<String> source, final String name)
			throws IOException {
		if (source.hasNext()) {
			final String line = source.next();
			if (line.isEmpty()) {
				throw new IllegalArgumentException(
					"File doesn't start by specifying which kind of table");
			}
			switch (line.toLowerCase().charAt(0)) {
			case 'q':
				if (source.hasNext()) {
					final String firstLine = source.next();
					final int rows;
					try {
						rows = Integer.parseInt(firstLine);
					} catch (final NumberFormatException except) {
						throw new IllegalArgumentException(
							"File doesn't start with number of rows of quadrants", except);
					}
					final List<String> items = new LinkedList<>();
					while (source.hasNext()) {
						items.add(source.next());
					}
					loadTable(name, new QuadrantTable(rows,
							items.toArray(new String[0])));
				} else {
					throw new IllegalArgumentException(
						"File doesn't start with number of rows of quadrants");
				}
				break;
			case 'r':
				final List<Pair<Integer, String>> listR = new ArrayList<>();
				while (source.hasNext()) {
					final String tableLine = source.next();
					final String[] splitted = tableLine.split(" ");
					if (splitted.length < 2) {
						if (tableLine.isEmpty()) {
							LOGGER.fine("Unexpected blank line");
						} else {
							LOGGER.severe("Line with no blanks, continuing ...");
							LOGGER.info("It was " + tableLine);
						}
					} else {
						final String left = splitted[0];
						final int leftNum;
						try {
							leftNum = Integer.parseInt(left);
						} catch (final NumberFormatException except) {
							throw new IllegalArgumentException(
								"Non-numeric data");
						}
						listR.add(Pair.with(leftNum,
							Stream.of(splitted).skip(1)
								.collect(Collectors.joining(" "))));
						loadTable(name, new RandomTable(
								listR.toArray(new Pair[0])));
					}
				}
				break;
			case 'c':
				if (source.hasNext()) {
					loadTable(name, new ConstantTable(source.next()));
				} else {
					throw new IllegalArgumentException(
						"constant value not present");
				}
				break;
			case 't':
				final List<Pair<String, String>> listT = new ArrayList<>();
				while (source.hasNext()) {
					final String tableLine = source.next();
					final String[] splitted = tableLine.split(" ");
					if (splitted.length < 2) {
						if (tableLine.isEmpty()) {
							LOGGER.fine("Unexpected blank line");
						} else {
							LOGGER.severe("Line with no blanks, coninuing ...");
							LOGGER.info(String.format("It was '%s'",
								tableLine));
						}
					} else {
						// N.B. first must be a recognized tile type
						// (including ver-1 types), but that's checked
						// by TerrainTable.
						listT.add(Pair.with(splitted[0],
							Stream.of(splitted).skip(1)
								.collect(Collectors.joining(" "))));
					}
				}
				loadTable(name, new TerrainTable(listT.toArray(new Pair[0])));
				break;
			default:
				throw new IllegalArgumentException(String.format(
					"unknown table type '%s' in file %s", line, name));
			}
		} else {
			throw new IllegalArgumentException(
				"File doesn't start by specifying which kind of table");
		}
	}

	/**
	 * Load a table from file into the runner. If the file<em>name</em> is
	 * provided, as a Tuple, the name is relative to a <pre>tables/</pre> directory.
	 */
	public void loadTableFromFile(final Path file) throws IOException {
		if (Files.exists(file)) {
			final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			loadTableFromDataStream(lines.iterator(), file.getFileName().toString());
		} else if (Files.exists(Paths.get("tables").resolve(file))) {
			final List<String> lines = Files.readAllLines(Paths.get("tables").resolve(file),
				StandardCharsets.UTF_8);
			loadTableFromDataStream(lines.iterator(), file.getFileName().toString());
		} else {
			throw new NoSuchFileException(file.toString());
		}
	}

	public void loadTableFromFile(final Class<?> cls, final String file) throws IOException {
		loadTableFromDataStream(FileContentsReader.readFileContents(
			cls, "tables/" + file).iterator(), file);
	}

	/**
	 * All possible results from the given table.
	 *
	 * @throws MissingTableException if that table has not been loaded
	 */
	public Iterable<String> getTableContents(final String table) throws MissingTableException {
		return getTable(table).getAllEvents();
	}

	/**
	 * Load all tables in the specified path into the runner.
	 */
	public void loadAllTables(final Path path) throws IOException {
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (final Path child : stream) {
				if (Files.isHidden(child)) { // TODO: Also exclude dotfiles on Windows?
					LOGGER.info(String.format(
						"%s looks like a hidden file, skipping ...",
						child.getFileName().toString()));
				} else {
					try {
						loadTableFromFile(child);
					} catch (final Exception except) {
						LOGGER.severe(String.format(
							"Error loading %s, continuing ...",
							child.getFileName().toString()));
						LOGGER.log(Level.FINE, "Details of that error:",
							except);
					}
				}
			}
		}
	}
}
