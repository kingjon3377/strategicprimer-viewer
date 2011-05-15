package view.map;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.viewer.Fortress;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;
import controller.map.MapReader;

/**
 * A viewer that generates an HTML view, for the really big maps.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class HTMLViewer {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(HTMLViewer.class
			.getName());

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map to create the HTML view for.
	 */
	// ESCA-JAVA0266:
	private HTMLViewer(final SPMap map) {
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(
				System.out));
		out.println("<html>");
		out.println("<head>");
		out.println("<title>Strategic Primer map view</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"1\">");
		final int rows = map.rows();
		final int cols = map.cols();
		for (int i = 0; i < rows; i++) {
			out.println("<tr>");
			for (int j = 0; j < cols; j++) {
				out.print("<td><img width=\"8\" height=\"8\" src=\"");
				out.print(STRING_MAP.get(map.getTile(i, j).getType()));
				out.print("\" alt=\"");
				out.print('(');
				out.print(i);
				out.print(", ");
				out.print(j);
				out.print("): ");
				out.print("Terrain: ");
				out.print(DESCRIPTIONS.get(map.getTile(i, j).getType()));
				out.print(anyForts(map.getTile(i, j)));
				out.print(anyUnits(map.getTile(i, j)));
				out.print(anyEvent(map.getTile(i, j)));
				out.println("\"></td>");
			}
			out.println("</tr>");
		}
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
		out.close();
	}

	/**
	 * @param args command-line arguments: the first is the map file, the rest are ignored.
	 */
	public static void main(final String[] args) {
		try {
			new HTMLViewer(new MapReader().readMap(args[0]));
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE, "XML parsing error", e);
			System.exit(1);
			return; // NOPMD;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error", e);
			System.exit(2);
			return; // NOPMD;
		}
	}

	/**
	 * A cache of terrain type image filenames.
	 */
	private static final Map<TileType, String> STRING_MAP = new EnumMap<TileType, String>(
			TileType.class);
	static {
		addStringToMap(TileType.Plains, "plains");
		addStringToMap(TileType.Ocean, "ocean");
		addStringToMap(TileType.TemperateForest, "tforest");
		addStringToMap(TileType.BorealForest, "bforest");
		addStringToMap(TileType.Desert, "desert");
		addStringToMap(TileType.Jungle, "jungle");
		addStringToMap(TileType.Mountain, "mountain");
		addStringToMap(TileType.Tundra, "tundra");
		addStringToMap(TileType.NotVisible, "notvisible");
	}

	/**
	 * Populate the filename cache.
	 * 
	 * @param type
	 *            A terrain type
	 * @param tile
	 *            The string to use as the base of the filename
	 */
	private static void addStringToMap(final TileType type, final String tile) {
		STRING_MAP.put(type, tile + ".png");
	}

	/**
	 * @param tile2
	 *            a tile
	 * @return a String representation of the units on the tile, if any,
	 *         preceded by a newline if there are any
	 */
	private static String anyUnits(final Tile tile2) {
		final StringBuffer retval = new StringBuffer("");
		if (!tile2.getUnits().isEmpty()) {
			retval.append('\n');
			for (final Unit u : tile2.getUnits()) {
				if (retval.length() > 1) {
					retval.append(", ");
				}
				retval.append(u.getType());
				retval.append(" (Player ");
				retval.append(u.getOwner());
				retval.append(')');
			}
		}
		return retval.toString();
	}

	/**
	 * @param tile2
	 *            a tile
	 * @return A string representation of any fortresses on the tile. If there
	 *         are any, it is preceded by a newline.
	 */
	private static String anyForts(final Tile tile2) {
		final StringBuffer retval = new StringBuffer(32);
		if (tile2.getForts().size() > 0) {
			retval.append("\nForts belonging to players ");
			for (final Fortress f : tile2.getForts()) {
				if (retval.charAt(retval.length() - 2) != 's') {
					retval.append(", ");
				}
				retval.append(f.getOwner());
			}
		}
		return retval.toString();
	}

	/**
	 * @param tile
	 *            a tile
	 * @return A string saying what the event on the tile is, if there is one,
	 *         or if not an empty string.
	 */
	private static String anyEvent(final Tile tile) {
		return (tile == null || tile.getEvent() == -1) ? "" : "\nEvent: "
				+ Integer.toString(tile.getEvent());
	}

	/**
	 * Descriptions of the types.
	 */
	private static final Map<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
	static {
		DESCRIPTIONS.put(TileType.BorealForest, "Boreal Forest");
		DESCRIPTIONS.put(TileType.Desert, "Desert");
		DESCRIPTIONS.put(TileType.Jungle, "Jungle");
		DESCRIPTIONS.put(TileType.Mountain, "Mountains");
		DESCRIPTIONS.put(TileType.NotVisible, "Unknown");
		DESCRIPTIONS.put(TileType.Ocean, "Ocean");
		DESCRIPTIONS.put(TileType.Plains, "Plains");
		DESCRIPTIONS.put(TileType.TemperateForest, "Temperate Forest");
		DESCRIPTIONS.put(TileType.Tundra, "Tundra");
	}
}
