package controller.exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.ConstantTable;
import model.exploration.EncounterTable;
import model.exploration.ExplorationRunner;
import model.exploration.LegacyTable;
import model.exploration.QuadrantTable;
import model.exploration.RandomTable;
import model.exploration.TerrainTable;
import model.map.TileType;
import util.LoadFile;
import util.Pair;

/**
 * A class to load encounter tables from file.
 * 
 * @author Jonathan Lovelace
 */
public final class TableLoader { // NOPMD
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(TableLoader.class.getName());
	/**
	 * An error-message string. Pulled out because it's so long.
	 */
	private static final String IO_ERR_STRING = "I/O error while reading table from file, "
			+ "continuing with what we've got so far ...";
	/**
	 * Extracted constant for clarity: If we split() a string "once", we tell
	 * split() to give us at most two pieces, and then test whether it gave us
	 * two or fewer pieces.
	 */
	private static final int SPLIT_ONCE = 2;

	/**
	 * Load a table from file. Format: first line specifies the kind of table
	 * (quadrant or random; first letter is the only one checked). Quadrant
	 * tables have number of rows on the next line, then all the items, one per
	 * line. Random tables have lines of the form X: Description, where X is the
	 * number at the bottom of the range of rolls the description applies to.
	 * Terrain tables have lines with each terrain type (as in the XML map
	 * format) followed by a description or event.
	 * 
	 * @param filename
	 *            the file containing the table.
	 * @throws IOException
	 *             on I/O error
	 * @return the table
	 * @throws FileNotFoundException
	 *             when file not found
	 */
	public EncounterTable loadTable(final String filename)
			throws FileNotFoundException, IOException { // NOPMD
		final BufferedReader reader = new LoadFile().doLoadFile(filename);
		try {
			return loadTable(reader);
		} catch (final IllegalArgumentException except) {
			if ("unknown table type".equals(except.getMessage())) {
				throw new IllegalArgumentException("File " + filename
						+ " specifies an unknown table type", except);
			} else {
				throw except;
			}
		} finally {
			reader.close();
		}
	}

	/**
	 * @param reader
	 *            the stream to read from
	 * @return the table constructed from the file
	 * @throws IOException
	 *             on I/O error or badly formed table.
	 */
	EncounterTable loadTable(final BufferedReader reader) throws IOException { // NOPMD
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException(
					"File doesn't start by specifying which kind of table.");
		} else {
			switch (line.charAt(0)) { // NOPMD
			case 'Q':
			case 'q':
				return loadQuadrantTable(reader); // NOPMD
			case 'R':
			case 'r':
				return loadRandomTable(reader); // NOPMD
			case 'C':
			case 'c':
				return loadConstantTable(reader); // NOPMD
			case 'L':
			case 'l':
				return loadLegacyTable(); // NOPMD
			case 'T':
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
	 * @param reader
	 *            the file descriptor
	 * @return the quadrant table the file describes.
	 * @throws IOException
	 *             on I/O error reading the number of rows
	 */
	public QuadrantTable loadQuadrantTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		if (line == null) {
			throw new IOException(
					"File doesn't start with the number of rows of quadrants");
		}
		final int rows = Integer.parseInt(line);
		final List<String> items = new LinkedList<String>();
		line = reader.readLine();
			try {
				while (line != null) {
					items.add(line);
				line = reader.readLine();
				}
			} catch (final IOException except) {
			Logger.getLogger(TableLoader.class.getName()).log(Level.SEVERE,
					IO_ERR_STRING, except);
		} finally {
		reader.close();
		}
		return new QuadrantTable(rows, items);
	}

	/**
	 * Load a RandomTable from file.
	 * 
	 * @param reader
	 *            the file descriptor
	 * @return the random-table the file describes.
	 * @throws IOException
	 *             on I/O error
	 */
	public RandomTable loadRandomTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();
		try {
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				Logger.getLogger(TableLoader.class.getName()).severe(
						"Line with no blanks, continuing ...");
			} else {
				list.add(Pair.of(Integer.parseInt(array[0]), array[1]));
			}
			line = reader.readLine();
		}
		} finally {
		reader.close();
		}
		return new RandomTable(list);
	}

	/**
	 * Load a TerrainTable from file.
	 * 
	 * @param reader
	 *            the file descriptor
	 * @return the terrain-table the file describes.
	 * @throws IOException
	 *             on I/O error.
	 */
	public TerrainTable loadTerrainTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<Pair<TileType, String>> list = new ArrayList<Pair<TileType, String>>();
		try {
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				Logger.getLogger(TableLoader.class.getName()).severe(
						"Line with no blanks, continuing ...");
			} else {
				list.add(Pair.of(TileType.getTileType(array[0]), array[1]));
			}
			line = reader.readLine();
		}
		} finally {
		reader.close();
		}
		return new TerrainTable(list);
	}

	/**
	 * Load a ConstantTable from file.
	 * 
	 * @param reader
	 *            the file descriptor
	 * @return the terrain-table the file describes.
	 * @throws IOException
	 *             on I/O error.
	 */
	public ConstantTable loadConstantTable(final BufferedReader reader)
			throws IOException {
		try {
			return new ConstantTable(reader.readLine());
		} finally {
			reader.close();
		}
	}

	/**
	 * Load a LegacyTable from file.
	 * 
	 * @return the table the file describes.
	 */
	public LegacyTable loadLegacyTable() {
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
	 * Try to load a table from file, but log the error and use the given backup
	 * if it fails.
	 * 
	 * @param filename
	 *            the file to load from
	 * @param defaultRows
	 *            the number of rows to use if loading fails
	 * @param defaultItems
	 *            a list of items to use if loading fails
	 * @return a valid table, from file if that works, using the default data if
	 *         not.
	 */
	public EncounterTable tryLoading(final String filename,
			final int defaultRows, final List<String> defaultItems) {
		try {
			return loadTable(filename); // NOPMD
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error loading the table from "
					+ filename, e);
			return new QuadrantTable(defaultRows, new LinkedList<String>(
					defaultItems));
		}
	}
	/**
	 * A list of tables to load.
	 */
	private final String[] defaultTableList = { "major_rock", "minor_rock",
			"boreal_major_tree", "temperate_major_tree", "main" };

	/**
	 * Loads the default set of tables.
	 * @param runner the runner to add them to
	 */
	public void loadDefaultTables(final ExplorationRunner runner) {
		for (final String table : defaultTableList) {
			runner.loadTable(table,
					tryLoading("tables/" + table, 2, createList(table, 4)));
		}
	}
	/**
	 * Create a list of strings, each beginning with a specified stem and ending
	 * with a sequential number.
	 * 
	 * @param stem
	 *            the string to begin each item with
	 * @param iterations
	 *            how many items should be in the list
	 * @return such a list
	 */
	private List<String> createList(final String stem, final int iterations) {
		if (iterations == 0) {
			return new ArrayList<String>(); // NOPMD
		} else {
			final List<String> list = createList(stem, iterations - 1);
			list.add(stem + iterations);
			return list;
		}
	}
	
	/**
	 * Load all tables in the specified path.
	 * 
	 * @param path
	 *            the directory to look in
	 * @param runner
	 *            the runner to add them to
	 */
	public void loadAllTables(final String path, final ExplorationRunner runner) {
		final TableLoader loader = new TableLoader();
		final File dir = new File(path);
		final String[] children = dir.list();
		if (children != null) {
			for (final String table : children) {
				try {
					runner.loadTable(table, loader.loadTable(path + '/' + table));
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
									+ ", probably a malformed file, possibly a Vim swap file",
							e);
				}
			}
		}
	}
}
