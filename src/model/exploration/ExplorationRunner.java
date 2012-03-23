package model.exploration;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.map.Tile;
import model.map.TileType;

/**
 * A class to create exploration results. The initial implementation is a bit
 * hackish, and should be generalized and improved.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ExplorationRunner { // NOPMD
	/**
	 * @param tile
	 *            a tile
	 * 
	 * @return what the owner of a fortress on the tile knows
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String defaultResults(final Tile tile) throws MissingTableException {
		final StringBuilder sb = new StringBuilder(// NOPMD
				"The primary rock type here is ");
		sb.append(getPrimaryRock(tile));
		sb.append(".\n");
		if (TileType.BorealForest.equals(tile.getTerrain())
				|| TileType.TemperateForest.equals(tile.getTerrain())) {
			sb.append("The main kind of tree is ");
			sb.append(getPrimaryTree(tile));
			sb.append(".\n");
		}
		return sb.toString();
	}

	/**
	 * The tables we know about.
	 */
	private final Map<String, EncounterTable> tables = new HashMap<String, EncounterTable>();

	/**
	 * Add a table. This is package-visibility so our test-case can use it.
	 * 
	 * @param name
	 *            The name to add the table under
	 * @param table
	 *            the table.
	 */
	public void loadTable(final String name, final EncounterTable table) { // NOPMD
		tables.put(name, table);
	}

	/**
	 * @param tile
	 *            a tile
	 * 
	 * @return the main kind of rock on the tile
	 * @throws MissingTableException if table missing
	 */
	public String getPrimaryRock(final Tile tile) throws MissingTableException {
		return getTable("major_rock").generateEvent(tile);
	}

	/**
	 * @param tile
	 *            a forest tile
	 * 
	 * @return the main kind of tree on the tile
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String getPrimaryTree(final Tile tile) throws MissingTableException {
		if (TileType.BorealForest.equals(tile.getTerrain())) {
			return getTable("boreal_major_tree").generateEvent(tile); // NOPMD
		} else if (TileType.TemperateForest.equals(tile.getTerrain())) {
			return getTable("temperate_major_tree").generateEvent(tile);
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
	 * @param table
	 *            the name of the table to consult
	 * @param tile
	 *            the tile to refer to
	 * 
	 * @return the result of the consultation
	 * @throws MissingTableException if the table is missing
	 */
	public String consultTable(final String table, final Tile tile) throws MissingTableException {
		return getTable(table).generateEvent(tile);
	}
	/**
	 * Get a table; guaranteed to return non-null (assuming a null wasn't explicitly added to the map).
	 * @param name the name of the table we want
	 * @return that table
	 * @throws MissingTableException if the table isn't in the map of tables.
	 */
	public EncounterTable getTable(final String name) throws MissingTableException {
		if (tables.containsKey(name)) {
			return tables.get(name);
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
	 * @param table
	 *            the name of the table to consult
	 * @param tile
	 *            the tile to refer to
	 * 
	 * @return the result of the consultation
	 * @throws MissingTableException on missing table
	 */
	public String recursiveConsultTable(final String table, final Tile tile) throws MissingTableException {
		String result = consultTable(table, tile);
		if (result == null) {
			throw new MissingTableException("Table " + table + " generated null result");
		}
		if (result.contains("#")) {
			final String[] split = result.split("#", 3);
			if (split.length < 3) {
				result = split[0] + recursiveConsultTable(split[1], tile);
			} else {
				result = split[0] + recursiveConsultTable(split[1], tile)
						+ split[2];
			}
		}
		return result;
	}

	/**
	 * Check that whether a table contains recursive calls to a table that
	 * doesn't exist.
	 * 
	 * @param table
	 *            the name of the table to consult
	 * 
	 * @return whether that table, or any table it calls, calls a table that
	 *         doesn't exist.
	 */
	public boolean recursiveCheck(final String table) { // $codepro.audit.disable
														// booleanMethodNamingConvention
		return recursiveCheck(table, new HashSet<String>());
	}

	/**
	 * Check whether a table contains recursive calls to a table that doesn't
	 * exist.
	 * 
	 * @param table
	 *            the name of the table to consult
	 * @param state
	 *            a Set to use to prevent infinite recursion
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
						if (value.contains("#")
								&& recursiveCheck(value.split("#", 3)[1], state)) {
							return true; // NOPMD
						}
					}
				} catch (MissingTableException e) {
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
	public boolean recursiveCheck() { // $codepro.audit.disable
										// booleanMethodNamingConvention
		final Set<String> state = new HashSet<String>();
		for (final String table : tables.keySet()) {
			if (recursiveCheck(table, state)) {
				return true; // NOPMD;
			}
		}
		return false;
	}

	/**
	 * Print the names of any tables that are called but don't exist yet.
	 * 
	 * @param ostream
	 *            The stream to print results on.
	 */
	public void verboseRecursiveCheck(final PrintStream ostream) {
		final Set<String> state = new HashSet<String>();
		for (final String table : tables.keySet()) {
			verboseRecursiveCheck(table, ostream, state);
		}
	}

	/**
	 * Print the names of any tables this one calls that don't exist yet.
	 * 
	 * @param table
	 *            the table to recursively check
	 * @param ostream
	 *            the stream to print results on
	 * @param state
	 *            to prevent infinite recursion.
	 */
	// ESCA-JAVA0049:
	private void verboseRecursiveCheck(final String table,
			final PrintStream ostream, final Set<String> state) {
		if (!state.contains(table)) {
			state.add(table);
			if (tables.keySet().contains(table)) {
				try {
					for (final String value : getTable(table).allEvents()) {
						if (value.contains("#")) {
							verboseRecursiveCheck(value.split("#", 3)[1], ostream,
									state);
						}
					}
				} catch (MissingTableException e) {
					ostream.println(e.getTable());
				}
			} else {
				ostream.println(table);
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
