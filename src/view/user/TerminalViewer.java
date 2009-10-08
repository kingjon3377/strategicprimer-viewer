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
 * A CLI to create a text version of the map from the XML.
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
	 * The entry point for this driver
	 * 
	 * @param args
	 *            Command-line arguments: The XML file name and the coordinates
	 *            of the point you want to blink.
	 */
	public static void main(final String[] args) {
		SPMap map;
		try {
			map = new XMLReader().getMap(args[0]);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			System.exit(1);
			return; // NOPMD;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			System.exit(2);
			return; // NOPMD;
		}
		char[][] textMap = new char[map.rows()][map.cols()];
		for (int i = 0; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				textMap[i][j] = getTerrainChar(map.getTile(i, j).getType());
			}
		}
		final StringBuilder sbuilder = new StringBuilder("");
		final int row = Integer.parseInt(args[1]);
		final int col = Integer.parseInt(args[2]);
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < map.cols(); j++) {
				sbuilder.append(textMap[i][j]);
			}
			sbuilder.append("\n");
		}
		for (int j = 0; j < col; j++) {
			sbuilder.append(textMap[row][j]);
		}
		sbuilder.append(ANSI.RED);
		sbuilder.append(ANSI.BLINK);
		sbuilder.append(textMap[row][col]);
		sbuilder.append(ANSI.SANE);
		for (int j = col + 1; j < map.cols(); j++) {
			sbuilder.append(textMap[row][j]);
		}
		sbuilder.append("\n");
		for (int i = row + 1; i < map.rows(); i++) {
			for (int j = 0; j < map.cols(); j++) {
				sbuilder.append(textMap[i][j]);
			}
			sbuilder.append("\n");
		}
		System.out.print(sbuilder.toString());
	}
	/**
	 * A mapping from terrain types to their single-character representations.
	 */
	private static final Map<TileType,Character> CHAR_MAP = new EnumMap<TileType,Character>(TileType.class);
	/**
	 * Set up the map.
	 */
	static {
		CHAR_MAP.put(TileType.Tundra,'T');
		CHAR_MAP.put(TileType.Desert,'D');
		CHAR_MAP.put(TileType.Mountain,'M');
		CHAR_MAP.put(TileType.BorealForest,'B');
		CHAR_MAP.put(TileType.TemperateForest,'F');
		CHAR_MAP.put(TileType.Ocean,'O');
		CHAR_MAP.put(TileType.Plains,'P');
		CHAR_MAP.put(TileType.Jungle,'J');
		CHAR_MAP.put(TileType.NotVisible, '_');
	}
	/**
	 * @param type A terrain type
	 * @return The single character representing that terrain type.
	 */
	private static char getTerrainChar(final TileType type) {
		return CHAR_MAP.containsKey(type) ? CHAR_MAP.get(type) : '_';
	}
}
