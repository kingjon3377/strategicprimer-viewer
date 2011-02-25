package controller.exploration;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.exploration.QuadrantTable;
import util.LoadFile;

/**
 * A class to load encounter tables from file.
 * 
 * @author Jonathan Lovelace
 */
public class TableLoader {
	/**
	 * Load a quadrant-based table from file. Format: number of rows, then all
	 * the items, one per line.
	 * 
	 * @param filename
	 *            the file containing the table.
	 * @throws IOException
	 *             on I/O error reading the number of rows
	 * @return the table
	 */
	public QuadrantTable loadQuadrantTable(final String filename)
			throws IOException {
		final BufferedReader reader = new LoadFile().doLoadFile(filename);
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
		return new QuadrantTable(rows, items);
	}
}
