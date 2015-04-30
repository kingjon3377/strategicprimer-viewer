package model.exploration.old;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.map.ITile;
import model.map.Point;
import model.map.TileType;
import util.NullCleaner;

/**
 * A class to create exploration results. The initial implementation is a bit
 * hackish, and should be generalized and improved.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationRunner { // NOPMD
	/**
	 * @param tile a tile
	 * @param point the tile's location
	 *
	 * @return what the owner of a fortress on the tile knows
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String defaultResults(final Point point, final ITile tile)
			throws MissingTableException {
		final StringBuilder sbuild = new StringBuilder(80)
				.append("The primary rock type here is ");
		sbuild.append(getPrimaryRock(point, tile));
		sbuild.append(".\n");
		if (TileType.BorealForest.equals(tile.getTerrain())
				|| TileType.TemperateForest.equals(tile.getTerrain())) {
			sbuild.append("The main kind of tree is ");
			sbuild.append(getPrimaryTree(point, tile));
			sbuild.append(".\n");
		}
		return NullCleaner.assertNotNull(sbuild.toString());
	}

	/**
	 * The tables we know about.
	 */
	private final Map<String, EncounterTable> tables = new HashMap<>();

	/**
	 * Add a table. This is package-visibility so our test-case can use it.
	 *
	 * @param name The name to add the table under
	 * @param table the table.
	 */
	public void loadTable(final String name, final EncounterTable table) { // NOPMD
		tables.put(name, table);
	}

	/**
	 * @param tile a tile
	 * @param point the location of the tile
	 *
	 * @return the main kind of rock on the tile
	 * @throws MissingTableException if table missing
	 */
	public String getPrimaryRock(final Point point, final ITile tile)
			throws MissingTableException {
		return getTable("major_rock").generateEvent(point, tile.getTerrain(), tile);
	}

	/**
	 * @param tile a forest tile
	 * @param point the location of the tile
	 *
	 * @return the main kind of tree on the tile
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String getPrimaryTree(final Point point, final ITile tile)
			throws MissingTableException {
		if (TileType.BorealForest.equals(tile.getTerrain())) {
			return getTable("boreal_major_tree").generateEvent(point, tile.getTerrain(), tile); // NOPMD
		} else if (TileType.TemperateForest.equals(tile.getTerrain())) {
			return getTable("temperate_major_tree").generateEvent(point, tile.getTerrain(), tile);
		} else {
			throw new IllegalArgumentException(
					"Only forests have primary trees");
		}
	}

	/**
	 * Consult a table. (Look up the given tile if it's a quadrant table, roll
	 * on it if it's a random-encounter table.) Note that the result may be the
	 * name of another table, which should then be consulted.
	 *
	 * @param table the name of the table to consult
	 * @param tile the tile to refer to
	 * @param point the location of the tile
	 *
	 * @return the result of the consultation
	 * @throws MissingTableException if the table is missing
	 */
	public String consultTable(final String table, final Point point,
			final ITile tile) throws MissingTableException {
		return getTable(table).generateEvent(point, tile.getTerrain(), tile);
	}

	/**
	 * Get a table; guaranteed to return non-null (assuming a null wasn't
	 * explicitly added to the map).
	 *
	 * @param name the name of the table we want
	 * @return that table
	 * @throws MissingTableException if the table isn't in the map of tables.
	 */
	public EncounterTable getTable(final String name)
			throws MissingTableException {
		if (tables.containsKey(name)) {
			final EncounterTable retval = tables.get(name);
			if (retval == null) {
				throw new MissingTableException(name);
			} else {
				return retval;
			}
		} else {
			throw new MissingTableException(name);
		}
	}

	/**
	 * Consult a table, and if the result indicates recursion, perform it.
	 * Recursion is indicated by hash-marks around the name of the table to
	 * call; results are undefined if there are more than two hash marks in any
	 * given string, or if either is at the beginning or the end of the string,
	 * since we use String.split .
	 *
	 * @param table the name of the table to consult
	 * @param tile the tile to refer to
	 * @param point the location of the tile
	 *
	 * @return the result of the consultation
	 * @throws MissingTableException on missing table
	 */
	public String recursiveConsultTable(final String table, final Point point,
			final ITile tile) throws MissingTableException {
		final String result = consultTable(table, point, tile);
		if (result.contains("#")) {
			final String[] split = result.split("#", 3);
			final String before = NullCleaner.assertNotNull(split[0]);
			final String middle = NullCleaner.assertNotNull(split[1]);
			final StringBuilder builder = new StringBuilder(100);
			builder.append(before);
			builder.append(recursiveConsultTable(middle, point, tile));
			if (split.length > 2) {
				builder.append(split[2]);
			}
			return NullCleaner.assertNotNull(builder.toString()); // NOPMD
		}
		return result;
	}

	/**
	 * Check that whether a table contains recursive calls to a table that
	 * doesn't exist.
	 *
	 * @param table the name of the table to consult
	 *
	 * @return whether that table, or any table it calls, calls a table that
	 *         doesn't exist.
	 */
	public boolean recursiveCheck(final String table) {
		return recursiveCheck(table, new HashSet<String>());
	}

	/**
	 * Check whether a table contains recursive calls to a table that doesn't
	 * exist.
	 *
	 * @param table the name of the table to consult
	 * @param state a Set to use to prevent infinite recursion
	 *
	 * @return whether the table, or any it calls, calls a table that doesn't
	 *         exist.
	 */
	// $codepro.audit.disable booleanMethodNamingConvention
	// ESCA-JAVA0049:
	private boolean recursiveCheck(final String table, final Set<String> state) {
		if (state.contains(table)) {
			return false; // NOPMD
		} else {
			state.add(table);
			if (tables.keySet().contains(table)) {
				try {
					for (final String value : getTable(table).allEvents()) {
						if (value.contains("#")) {
							final String splitVal = value.split("#", 3)[1];
							if (splitVal != null
									&& recursiveCheck(splitVal, state)) {
								return true; // NOPMD
							}
						}
					}
				} catch (final MissingTableException e) {
					return true; // NOPMD
				}
				return false; // NOPMD
			} else {
				return true;
			}
		}
	}

	/**
	 * Check whether any table contains recursive calls to a table that doesn't
	 * exist.
	 *
	 *
	 * @return whether any table contains recursive calls to a nonexistent
	 *         table.
	 */
	public boolean recursiveCheck() {
		final Set<String> state = new HashSet<>(); // NOPMD
		for (final String table : tables.keySet()) {
			if (table != null && recursiveCheck(table, state)) {
				return true; // NOPMD;
			}
		}
		return false;
	}

	/**
	 * Print the names of any tables that are called but don't exist yet.
	 *
	 * @param ostream The stream to print results on.
	 * @throws IOException on I/O error writing to stream
	 */
	public void verboseRecursiveCheck(final Appendable ostream) throws IOException {
		final Set<String> state = new HashSet<>(); // NOPMD
		for (final String table : tables.keySet()) {
			if (table != null) {
				verboseRecursiveCheck(table, ostream, state);
			}
		}
	}

	/**
	 * Print the names of any tables this one calls that don't exist yet.
	 *
	 * @param table the table to recursively check
	 * @param ostream the stream to print results on
	 * @param state to prevent infinite recursion.
	 * @throws IOException on I/O error writing to the stream
	 */
	// ESCA-JAVA0049:
	private void verboseRecursiveCheck(final String table,
			final Appendable ostream, final Set<String> state) throws IOException {
		if (!state.contains(table)) {
			state.add(table);
			if (tables.keySet().contains(table)) {
				try {
					for (final String value : getTable(table).allEvents()) {
						if (value.contains("#")) {
							final String splitVal = value.split("#", 3)[1];
							if (splitVal != null) {
								verboseRecursiveCheck(splitVal,
										ostream, state);
							}
						}
					}
				} catch (final MissingTableException e) {
					ostream.append(e.getTable());
				}
			} else {
				ostream.append(table);
			}
		}
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationRunner";
	}
}
