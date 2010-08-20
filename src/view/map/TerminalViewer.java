package view.map;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.viewer.SPMap;
import model.viewer.TileType;

import org.xml.sax.SAXException;

import controller.map.XMLReader;

/**
 * A CLI to create a text version of the map from the XML, coloring a specified
 * point red.
 * 
 * @author Jonathan Lovelace
 */
public class TerminalViewer {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(TerminalViewer.class
			.getName());
	/**
	 * The text map
	 */
	private final char[][] textMap;

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
	public TerminalViewer(final SPMap map, final int row, final int col) {
		textMap = new char[map.rows()][map.cols()];
		createTextMap(map);
		final StringBuilder sbuilder = new StringBuilder("");
		for (int i = 0; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				if (i == row && j == col) {
					sbuilder.append(ANSI.RED);
					sbuilder.append(ANSI.BLINK);
					sbuilder.append(textMap[i][j]);
					sbuilder.append(ANSI.SANE);
				} else {
					sbuilder.append(textMap[i][j]);
				}
			}
			sbuilder.append("\n");
		}
		// ESCA-JAVA0266:
		System.out.print(sbuilder.toString());
	}

	/**
	 * Create the text map
	 * 
	 * @param map
	 *            The map the text map represents
	 */
	private void createTextMap(final SPMap map) {
		for (int i = 0; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				textMap[i][j] = getTerrainChar(map.getTile(i, j).getType());
			}
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
			new TerminalViewer(new XMLReader().getMap(args[0]), Integer
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
	private static final Map<TileType, Character> CHAR_MAP = new EnumMap<TileType, Character>(
			TileType.class);
	/**
	 * Set up the map.
	 */
	static {
		CHAR_MAP.put(TileType.Tundra, 'T');
		CHAR_MAP.put(TileType.Desert, 'D');
		CHAR_MAP.put(TileType.Mountain, 'M');
		CHAR_MAP.put(TileType.BorealForest, 'B');
		CHAR_MAP.put(TileType.TemperateForest, 'F');
		CHAR_MAP.put(TileType.Ocean, 'O');
		CHAR_MAP.put(TileType.Plains, 'P');
		CHAR_MAP.put(TileType.Jungle, 'J');
		CHAR_MAP.put(TileType.NotVisible, '_');
	}

	/**
	 * @param type
	 *            A terrain type
	 * @return The single character representing that terrain type.
	 */
	private static char getTerrainChar(final TileType type) {
		return CHAR_MAP.containsKey(type) ? CHAR_MAP.get(type) : '_';
	}
}
