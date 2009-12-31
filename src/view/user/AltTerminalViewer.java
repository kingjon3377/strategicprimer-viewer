package view.user;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.SPMap;
import model.TileType;

import org.xml.sax.SAXException;

import controller.XMLReader;

/**
 * A CLI to create a colored text version of the map from the XML, blinking a
 * specified point.
 * 
 * @author Jonathan Lovelace
 */
public final class AltTerminalViewer {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(AltTerminalViewer.class
			.getName());
	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map to create the view for
	 * @param row
	 *            the row of the point to highlight
	 * @param col
	 *            the column of the point to highlight
	 */
	// ESCA-JAVA0266:
	private AltTerminalViewer(final SPMap map, final int row, final int col) {
		for (int i = 0; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				if (i == row && j == col) {
					System.out.print(ANSI.BLINK);
				}
				System.out.print(getTerrainChar(map.getTile(i, j).getType()));
				System.out.print(ANSI.SANE);
			}
			System.out.println();
		}
	}

	/**
	 * The entry point for this driver
	 * 
	 * @param args
	 *            Command-line arguments: The XML file name and the coordinates
	 *            of the point you want to blink.
	 */
	public static void main(final String[] args) {
		try {
			new AltTerminalViewer(new XMLReader().getMap(args[0]), Integer
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

	/**
	 * A mapping from terrain types to their single-character representations.
	 */
	private static final Map<TileType, String> CHAR_MAP = new EnumMap<TileType, String>(
			TileType.class);
	/**
	 * Set up the map.
	 */
	static {
		CHAR_MAP.put(TileType.Tundra, ANSI.BACKGROUND_WHITE + ANSI.BLACK + 'T');
		CHAR_MAP.put(TileType.Desert, ANSI.BACKGROUND_BLACK + ANSI.YELLOW + 'D');
		CHAR_MAP.put(TileType.Mountain, ANSI.BACKGROUND_BLACK + ANSI.RED + 'M');
		CHAR_MAP.put(TileType.BorealForest, ANSI.BACKGROUND_BLACK + ANSI.CYAN + 'B');
		CHAR_MAP.put(TileType.TemperateForest, ANSI.BACKGROUND_BLACK + ANSI.GREEN + 'F');
		CHAR_MAP.put(TileType.Ocean, ANSI.BACKGROUND_BLACK + ANSI.BLUE + 'O');
		CHAR_MAP.put(TileType.Plains, ANSI.BACKGROUND_BLACK + ANSI.MAGENTA + 'P');
		CHAR_MAP.put(TileType.Jungle, ANSI.BACKGROUND_BLACK + ANSI.HIGH_INTENSITY + ANSI.WHITE + 'J');
		CHAR_MAP.put(TileType.NotVisible, Character.toString('_'));
	}

	/**
	 * @param type
	 *            A terrain type
	 * @return The single character representing that terrain type.
	 */
	private static String getTerrainChar(final TileType type) {
		return CHAR_MAP.containsKey(type) ? CHAR_MAP.get(type) : Character.toString('_');
	}
}
