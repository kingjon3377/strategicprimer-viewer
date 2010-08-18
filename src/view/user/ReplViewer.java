package view.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.viewer.Fortress;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.Unit;

import org.xml.sax.SAXException;

import controller.XMLReader;

/**
 * A viewer to let the user explore the map without being overwhelmed by
 * extraneous information.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ReplViewer {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(ReplViewer.class
			.getName());

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map to create the view for
	 * @param row
	 *            the row of the user's starting point
	 * @param col
	 *            the column of the user's starting point
	 */
	private ReplViewer(final SPMap map, final int row, final int col) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		int iRow = row, iCol = col;
		Tile tile = map.getTile(row, col);
		printTile(tile);
		try {
			String input = reader.readLine();
			while (input != null && input.length() > 0) {
				if (input.charAt(0) == 'q') {
					break;
				} else {
					switch (input.charAt(0)) {
					case 'n':
						iRow--;
						break;
					case 's':
						iRow++;
						break;
					case 'e':
						iCol++;
						break;
					case 'w':
						iCol--;
						break;
					default:
						break;
					}
				}
				tile = map.getTile(iRow, iCol);
				printTile(tile);
				input = reader.readLine();
			}
			reader.close();
		} catch (IOException except) {
			LOGGER.log(Level.SEVERE, "I/O exception", except);
		}
	}

	/**
	 * Print a tile.
	 * 
	 * @param tile
	 *            the tile to print
	 */
	// ESCA-JAVA0266:
	private static void printTile(final Tile tile) {
		System.out.print("Tile (");
		System.out.print(tile.getRow());
		System.out.print(", ");
		System.out.print(tile.getCol());
		System.out.println("):");
		System.out.print("Tile type: ");
		System.out.println(tile.getType().toString());
		if (!tile.getForts().isEmpty()) {
			System.out.println("Fortresses on this tile:");
			for (Fortress fort : tile.getForts()) {
				System.out.println(fort);
			}
		}
		if (!tile.getUnits().isEmpty()) {
			System.out.println("Units on this tile:");
			for (Unit unit : tile.getUnits()) {
				System.out.println(unit);
			}
		}
		if (tile.getEvent() != -1) {
			System.out.print("Event on this tile: ");
			System.out.println(tile.getEvent());
		}
	}

	/**
	 * @param args
	 *            command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new ReplViewer(new XMLReader().getMap(args[0]), Integer
					.parseInt(args[1]), Integer.parseInt(args[2]));
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			System.exit(1);
			return; // NOPMD;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			System.exit(2);
			return; // NOPMD;
		}
	}
}
