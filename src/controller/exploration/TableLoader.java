package controller.exploration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.EncounterTable;
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
public class TableLoader {
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
	 * @throws FileNotFoundException when file not found
	 */
	public EncounterTable loadTable(final String filename) throws FileNotFoundException, IOException {
		final BufferedReader reader = new LoadFile().doLoadFile(filename);
		final String line = reader.readLine();
		if (line == null) {
			throw new IOException("File doesn't start by specifying which kind of table.");
		} else if (line.charAt(0) == 'Q' || line.charAt(0) == 'q') {
			return loadQuadrantTable(reader); // NOPMD
		} else if (line.charAt(0) == 'R' || line.charAt(0) == 'r') {
			return loadRandomTable(reader); // NOPMD
		} else if (line.charAt(0) == 'T' || line.charAt(0) == 't') {
			return loadTerrainTable(reader);
		} else {
			throw new IllegalArgumentException("File specifies an unknown table type");
		}
	}
	/**
	 * Load a QuadrantTable from file.
	 * @param reader the file descriptor
	 * @return the quadrant table the file describes.
	 * @throws IOException on I/O error reading the number of rows
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
			} catch (IOException except) {
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
	 * @param reader the file descriptor
	 * @return the random-table the file describes. 
	 * @throws IOException on I/O error
	 */
	public RandomTable loadRandomTable(final BufferedReader reader) throws IOException {
		String line = reader.readLine();
		final List<Pair<Integer,String>> list = new ArrayList<Pair<Integer, String>>();
		while (line != null) {
			final String[] array = line.split(" ", 2);
			if (array.length < 2) {
				Logger.getLogger(TableLoader.class.getName()).severe("Line with no blanks, continuing ...");
			} else {
				list.add(Pair.of(Integer.parseInt(array[0]),array[1]));
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
			final String[] array = line.split(" ", 2);
			if (array.length < 2) {
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
}
