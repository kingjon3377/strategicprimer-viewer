package model.exploration.old;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import model.map.MapDimensions;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import util.TypesafeLogger;

/**
 * A class to create exploration results. The initial implementation is a bit hackish, and
 * should be generalized and improved.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationRunner {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(ExplorationRunner.class);
	/**
	 * The tables we know about.
	 */
	private final Map<String, EncounterTable> tables = new HashMap<>();

	/**
	 * Get the "default results" (primary rock and primary forest) for the given point.
	 * @param point         the tile's location
	 * @param terrain       the terrain at the location
	 * @param fixtures      any fixtures at the location
	 * @param mapDimensions the dimensions of the map
	 * @return what the owner of a fortress on the tile knows
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String defaultResults(final Point point, final TileType terrain,
								 final Stream<TileFixture> fixtures,
								 final MapDimensions mapDimensions)
			throws MissingTableException {
		final StringBuilder builder = new StringBuilder(80);
		try (final Formatter formatter = new Formatter(builder)) {
			formatter.format("The primary rock type here is %s.%n",
					getPrimaryRock(point, terrain, fixtures, mapDimensions));
			if ((TileType.BorealForest == terrain) ||
						(TileType.TemperateForest == terrain)) {
				formatter.format("The main kind of tree is %s.%n",
						getPrimaryTree(point, terrain, fixtures, mapDimensions));
			}
		}
		return builder.toString();
	}

	/**
	 * Add a table. This is package-visibility so our test-case can use it.
	 *
	 * @param name  The name to add the table under
	 * @param table the table.
	 */
	public void loadTable(final String name, final EncounterTable table) {
		tables.put(name, table);
	}

	/**
	 * Get the primary rock for the given location.
	 * @param point         the location of the tile
	 * @param terrain       the terrain of the tile
	 * @param fixtures      any fixtures on the tile
	 * @param mapDimensions the dimensions of the map
	 * @return the main kind of rock on the tile
	 * @throws MissingTableException if table missing
	 */
	public String getPrimaryRock(final Point point, final TileType terrain,
								 final Stream<TileFixture> fixtures,
								 final MapDimensions mapDimensions)
			throws MissingTableException {
		return getTable("major_rock").generateEvent(point,
				terrain, fixtures, mapDimensions);
	}

	/**
	 * Get the primary forest for the given location.
	 * @param point         the location of the tile
	 * @param terrain       the tile type
	 * @param fixtures      any fixtures on the tile
	 * @param mapDimensions the dimensions of the map
	 * @return the main kind of tree on the tile
	 * @throws MissingTableException on missing table
	 */
	@SuppressWarnings("deprecation")
	public String getPrimaryTree(final Point point, final TileType terrain,
								 final Stream<TileFixture> fixtures,
								 final MapDimensions mapDimensions)
			throws MissingTableException {
		if (TileType.BorealForest == terrain) {
			return getTable("boreal_major_tree").generateEvent(point,
					TileType.BorealForest, fixtures, mapDimensions);
		} else if (TileType.TemperateForest == terrain) {
			return getTable("temperate_major_tree").generateEvent(point,
					TileType.TemperateForest, fixtures, mapDimensions);
		} else {
			throw new IllegalArgumentException("Only forests have primary trees");
		}
	}

	/**
	 * Consult a table. (Look up the given tile if it's a quadrant table, roll on it if
	 * it's a random-encounter table.) Note that the result may be the name of another
	 * table, which should then be consulted.
	 *
	 * @param table         the name of the table to consult
	 * @param point         the location of the tile
	 * @param terrain       the tile type
	 * @param fixtures      any fixtures on the tile
	 * @param mapDimensions the dimensions of the map
	 * @return the result of the consultation
	 * @throws MissingTableException if the table is missing
	 */
	public String consultTable(final String table, final Point point,
							   final TileType terrain, final Stream<TileFixture>
															   fixtures,
							   final MapDimensions mapDimensions)
			throws MissingTableException {
		return getTable(table).generateEvent(point, terrain, fixtures, mapDimensions);
	}

	/**
	 * Get a table; guaranteed to return non-null (assuming a null wasn't explicitly
	 * added
	 * to the map).
	 *
	 * @param name the name of the table we want
	 * @return that table
	 * @throws MissingTableException if the table isn't in the map of tables.
	 */
	public EncounterTable getTable(final String name)
			throws MissingTableException {
		if (tables.containsKey(name)) {
			return tables.get(name);
		} else {
			throw new MissingTableException(name);
		}
	}


	/**
	 * Consult a table, and if the result indicates recursion, perform it. Recursion is
	 * indicated by hash-marks around the name of the table to call; results are
	 * undefined
	 * if there are more than two hash marks in any given string, or if either is at the
	 * beginning or the end of the string, since we use String.split .
	 *
	 * @param table         the name of the table to consult
	 * @param point         the location of the tile
	 * @param terrain       the tile type
	 * @param fixtures      any fixtures on the tile
	 * @param mapDimensions the dimensions of the map
	 * @return the result of the consultation
	 * @throws MissingTableException on missing table
	 */
	public String recursiveConsultTable(final String table, final Point point,
										final TileType terrain,
										final Stream<TileFixture> fixtures,
										final MapDimensions mapDimensions)
			throws MissingTableException {
		final String result =
				consultTable(table, point, terrain, fixtures, mapDimensions);
		if (result.contains("#")) {
			final String[] split = result.split("#", 3);
			final String before = split[0];
			final String middle = split[1];
			final StringBuilder builder = new StringBuilder(100);
			builder.append(before);
			builder.append(recursiveConsultTable(middle, point, terrain, fixtures,
					mapDimensions));
			if (split.length > 2) {
				builder.append(split[2]);
			}
			return builder.toString();
		}
		return result;
	}

	/**
	 * Check that whether a table contains recursive calls to a table that doesn't exist.
	 *
	 * @param table the name of the table to consult
	 * @return whether that table, or any table it calls, calls a table that doesn't
	 * exist.
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public boolean recursiveCheck(final String table) {
		return recursiveCheck(table, new HashSet<>());
	}

	/**
	 * Check whether a table contains recursive calls to a table that doesn't exist.
	 *
	 * @param table the name of the table to consult
	 * @param state a Set to use to prevent infinite recursion
	 * @return whether the table, or any it calls, calls a table that doesn't exist.
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	private boolean recursiveCheck(final String table, final Set<String> state) {
		if (state.contains(table)) {
			return false;
		} // else
		state.add(table);
		if (tables.keySet().contains(table)) {
			try {
				for (final String value : getTable(table).allEvents()) {
					if (value.contains("#")) {
						if (recursiveCheck(value.split("#", 3)[1], state)) {
							return true;
						}
					}
				}
			} catch (final MissingTableException e) {
				LOGGER.log(Level.INFO, "Missing table " + table, e);
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check whether any table contains recursive calls to a table that doesn't exist.
	 *
	 * @return whether any table contains recursive calls to a nonexistent table.
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public boolean recursiveCheck() {
		final Set<String> state = new HashSet<>();
		for (final String table : tables.keySet()) {
			if (recursiveCheck(table, state)) {
				return true;
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
		final Set<String> state = new HashSet<>();
		for (final String table : tables.keySet()) {
			verboseRecursiveCheck(table, ostream, state);
		}
	}

	/**
	 * Print the names of any tables this one calls that don't exist yet.
	 *
	 * @param table   the table to recursively check
	 * @param ostream the stream to print results on
	 * @param state   to prevent infinite recursion.
	 * @throws IOException on I/O error writing to the stream
	 */
	private void verboseRecursiveCheck(final String table, final Appendable ostream,
									   final Set<String> state) throws IOException {
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
				} catch (final MissingTableException e) {
					ostream.append(e.getTable());
				}
			} else {
				ostream.append(table);
			}
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationRunner";
	}
}
