package controller.exploration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.ConstantTable;
import model.exploration.EncounterTable;
import model.exploration.LegacyTable;
import model.exploration.QuadrantTable;
import model.exploration.RandomTable;
import model.exploration.TerrainTable;
import model.viewer.TileType;
import util.LoadFile;
import util.Pair;

/**
 * A class to load encounter tables from file.
 * 
 * @author Jonathan Lovelace
 */
public class TableLoader { // NOPMD
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
			final EncounterTable table = loadTable(reader);
			reader.close();
			return table;
		} catch (final IllegalArgumentException except) {
			reader.close();
			if ("unknown table type".equals(except.getMessage())) {
				throw new IllegalArgumentException("File " + filename
						+ " specifies an unknown table type", except);
			} else {
				throw except;
			}
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
		while (line != null) {
			items.add(line);
			try {
				line = reader.readLine();
			} catch (final IOException except) {
				Logger.getLogger(TableLoader.class.getName())
						.log(Level.SEVERE,
								"I/O error while reading table from file, continuing with what we've got so far ...",
								except);
				break;
			}
		}
		reader.close();
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
		reader.close();
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
		reader.close();
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
		final String string = reader.readLine();
		reader.close();
		return new ConstantTable(string);
	}

	/**
	 * Load a LegacyTable from file.
	 * 
	 * @return the table the file describes.
	 */
	public LegacyTable loadLegacyTable() {
		return new LegacyTable();
	}
}
