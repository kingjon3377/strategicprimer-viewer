package controller.exploration;

import model.exploration.old.ConstantTable;
import model.exploration.old.EncounterTable;
import model.exploration.old.ExplorationRunner;
import model.exploration.old.LegacyTable;
import model.exploration.old.QuadrantTable;
import model.exploration.old.RandomTable;
import model.exploration.old.TerrainTable;
import model.map.TileType;
import util.ComparablePair;
import util.NullCleaner;
import util.Pair;
import util.ResourceInputStream;
import util.TypesafeLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to load encounter tables from file.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class TableLoader { // NOPMD
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(TableLoader.class);
	/**
	 * An error-message string. Pulled out because it's so long.
	 */
	private static final String IO_ERR_STRING =
			"I/O error while reading table, continuing with what we have so far ...";
	/**
	 * Extracted constant for clarity: If we split() a string "once", we tell split() to
	 * give us at most two pieces, and then test whether it gave us two or fewer pieces.
	 */
	private static final int SPLIT_ONCE = 2;

	/**
	 * Load a table from file. Format: first line specifies the kind of table
	 * (quadrant or
	 * random; first letter is the only one checked). Quadrant tables have number of rows
	 * on the next line, then all the items, one per line. Random tables have lines of
	 * the
	 * form X: Description, where X is the number at the bottom of the range of rolls the
	 * description applies to. Terrain tables have lines with each terrain type (as in
	 * the
	 * XML map format) followed by a description or event.
	 *
	 * @param filename the file containing the table.
	 * @return the table
	 * @throws IOException when file not found or on other I/O error
	 */
	public static EncounterTable loadTable(final String filename)
			throws IOException {
		try (final BufferedReader reader = new BufferedReader(
				                                                     new
						                                                     InputStreamReader(new ResourceInputStream(filename)))) {
			return loadTableFromStream(reader);
		} catch (final IllegalArgumentException except) {
			if ("unknown table type".equals(except.getMessage())) {
				throw new IllegalArgumentException("File " + filename
						                                   +
						                                   " specifies an unknown table " +
						                                   "type",
						                                  except);
			} else {
				throw except;
			}
		}
	}

	/**
	 * @param reader the stream to read from
	 * @return the table constructed from the file
	 * @throws IOException on I/O error or badly formed table.
	 */
	static EncounterTable loadTableFromStream(final BufferedReader reader) // NOPMD
			throws IOException {
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException(
					                     "File doesn't start by specifying which kind of" +
							                     " table.");
		} else {
			final char cmd = Character.toLowerCase(line.charAt(0));
			switch (cmd) { // NOPMD
			case 'q':
				return loadQuadrantTable(reader); // NOPMD
			case 'r':
				return loadRandomTable(reader); // NOPMD
			case 'c':
				return loadConstantTable(reader); // NOPMD
			case 'l':
				return loadLegacyTable(); // NOPMD
			case 't':
				return loadTerrainTable(reader);
			default:
				throw new IllegalArgumentException("unknown table type");
			}
		}
	}

	/**
	 * Load a QuadrantTable from file.
	 *
	 * @param reader the file descriptor
	 * @return the quadrant table the file describes.
	 * @throws IOException on I/O error reading the number of rows
	 */
	public static EncounterTable loadQuadrantTable(final BufferedReader reader)
			throws IOException {
		final String firstLine = reader.readLine();
		if (firstLine == null) {
			throw new IOException(
					                     "File doesn't start with the number of rows of " +
							                     "quadrants");
		}
		final int rows;
		try {
			rows = NumberFormat.getIntegerInstance().parse(firstLine).intValue();
		} catch (final NumberFormatException | ParseException except) {
			throw new IOException(
					                     "File doesn't start with number of rows of " +
							                     "quadrants",
					                     except);
		}
		final List<String> items = new LinkedList<>();
		try {
			for (String line = reader.readLine(); line != null;
					line = reader.readLine()) {
				items.add(line);
			}
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, IO_ERR_STRING, except);
		}
		return new QuadrantTable(rows, items);
	}

	/**
	 * Load a RandomTable from file.
	 *
	 * @param reader the file descriptor
	 * @return the random-table the file describes.
	 * @throws IOException on I/O error
	 */
	public static EncounterTable loadRandomTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<ComparablePair<Integer, String>> list = new ArrayList<>();
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				LOGGER.severe("Line with no blanks, continuing ...");
			} else {
				try {
					final String left = array[0];
					final String right = array[1];
					final Integer leftNum = Integer.valueOf(left);
					list.add(ComparablePair.of(NullCleaner.assertNotNull(leftNum),
							NullCleaner.assertNotNull(right)));
				} catch (final NumberFormatException except) {
					throw new IOException("Non-numeric data", except);
				}
			}
			line = reader.readLine();
		}
		return new RandomTable(list);
	}

	/**
	 * Load a TerrainTable from file.
	 *
	 * @param reader the file descriptor
	 * @return the terrain-table the file describes.
	 * @throws IOException on I/O error.
	 */
	public static EncounterTable loadTerrainTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<Pair<TileType, String>> list = new ArrayList<>();
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				LOGGER.severe("Line with no blanks, continuing ...");
			} else {
				list.add(Pair.of(TileType.getTileType(NullCleaner
						                                      .assertNotNull(array[0])),
						NullCleaner
								.assertNotNull(array[1])));
			}
			line = reader.readLine();
		}
		return new TerrainTable(list);
	}

	/**
	 * Load a ConstantTable from file.
	 *
	 * @param reader the file descriptor
	 * @return the terrain-table the file describes.
	 * @throws IOException on I/O error.
	 */
	public static EncounterTable loadConstantTable(final BufferedReader reader)
			throws IOException {
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException("read a null line");
		}
		return new ConstantTable(line);
	}

	/**
	 * Load a LegacyTable from file.
	 *
	 * @return the table the file describes.
	 */
	public static EncounterTable loadLegacyTable() {
		return new LegacyTable();
	}

	/**
	 * @return a String representation of this class
	 */
	@Override
	public String toString() {
		return "TableLoader";
	}

	/**
	 * Load all tables in the specified path.
	 *
	 * @param path   the directory to look in
	 * @param runner the runner to add them to
	 */
	public static void loadAllTables(final String path,
	                                 final ExplorationRunner runner) {
		final File dir = new File(path);
		final String[] children = dir.list();
		if (children != null) {
			for (final String table : children) {
				if ('.' == table.charAt(0) || table.contains("/.")) {
					LOGGER.info(table
							            + " looks like a hidden file, skipping ...");
					continue;
				}
				try {
					runner.loadTable(table, loadTable(path + '/' + table));
				} catch (final FileNotFoundException e) {
					LOGGER.log(Level.SEVERE, "File " + table + " not found", e);
				} catch (final IOException e) {
					LOGGER.log(Level.SEVERE,
							"I/O error while parsing " + table, e);
				} catch (final IllegalArgumentException e) {
					LOGGER.log(
							Level.SEVERE,
							"Illegal argument while parsing "
									+ table
									+ ", probably a malformed file",
							e);
				}
			}
		}
	}
}
