package controller.exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * A class to load encounter tables from file.
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
	private static final String IO_ERR_STRING = NullCleaner
			.assertNotNull(new StringBuilder(
					"I/O error while reading table from file, ").append(
					"continuing with what we've got so far ...").toString());
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
	 * @param filename the file containing the table.
	 * @return the table
	 * @throws IOException when file not found or on other I/O error
	 */
	public static EncounterTable loadTable(final String filename)
			throws IOException {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ResourceInputStream(filename)))) {
			return loadTableFromStream(reader);
		} catch (final IllegalArgumentException except) {
			if ("unknown table type".equals(except.getMessage())) {
				throw new IllegalArgumentException("File " + filename
						+ " specifies an unknown table type", except);
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
					"File doesn't start by specifying which kind of table.");
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
	public static QuadrantTable loadQuadrantTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		if (line == null) {
			throw new IOException(
					"File doesn't start with the number of rows of quadrants");
		}
		final int rows = Integer.parseInt(line);
		final List<String> items = new LinkedList<>();
		line = reader.readLine();
		try {
			while (line != null) {
				items.add(line);
				line = reader.readLine();
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
	public static RandomTable loadRandomTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<ComparablePair<Integer, String>> list = new ArrayList<>();
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				LOGGER.severe("Line with no blanks, continuing ...");
			} else {
				list.add(ComparablePair.of(
						NullCleaner.assertNotNull(Integer.valueOf(array[0])),
						NullCleaner.assertNotNull(array[1])));
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
	public static TerrainTable loadTerrainTable(final BufferedReader reader)
			throws IOException {
		String line = reader.readLine();
		final List<Pair<TileType, String>> list = new ArrayList<>();
		while (line != null) {
			final String[] array = line.split(" ", SPLIT_ONCE);
			if (array.length < SPLIT_ONCE) {
				LOGGER.severe("Line with no blanks, continuing ...");
			} else {
				list.add(Pair.of(TileType.getTileType(NullCleaner
						.assertNotNull(array[0])), NullCleaner
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
	public static ConstantTable loadConstantTable(final BufferedReader reader)
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
	 *
	 * @return the table the file describes.
	 */
	public static LegacyTable loadLegacyTable() {
		return new LegacyTable();
	}

	/**
	 *
	 * @return a String representation of this class
	 */
	@Override
	public String toString() {
		return "TableLoader";
	}

	/**
	 * Load all tables in the specified path.
	 *
	 * @param path the directory to look in
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
