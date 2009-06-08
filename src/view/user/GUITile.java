package view.user;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import model.Fortress;
import model.Tile;
import model.Unit;
import model.Tile.TileType;

/**
 * A GUI representation of a tile. Information about what's on the tile should
 * be indicated by a small icon or by tooltip text.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class GUITile extends JLabel {
	/**
	 * A buffer to avoid string concatenations.
	 */
	private final transient StringBuffer retval; 
	/**
	 * 
	 */
	private static final long serialVersionUID = 4047750632787337702L;

	/**
	 * Constructor.
	 * 
	 * @param tile
	 *            the tile this will represent
	 */
	public GUITile(final Tile tile) {
		super(new ImageIcon(ViewerFrame.getFrame().getToolkit().createImage(
				getImageURL(tile.getType()))));
		setToolTipText("Terrain: " + terrainText(tile.getType()) + anyForts(tile)
				+ anyUnits(tile));
		retval = new StringBuffer();
	}

	/**
	 * @param tile2
	 *            a tile
	 * @return a String representation of the units on the tile, if any,
	 *         preceded by a newline if there are any
	 */
	private String anyUnits(final Tile tile2) {
		retval.setLength(0);
		if (tile2.getUnits().size() > 0) {
			retval.append('\n');
			for (Unit u : tile2.getUnits()) {
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
	private String anyForts(final Tile tile2) {
		retval.setLength(0);
		if (tile2.getForts().size() > 0) {
			retval.append("\nForts belonging to players ");
			for (Fortress f : tile2.getForts()) {
				if (retval.charAt(retval.length()) - 2 != 's') {
					retval.append(", ");
				}
				retval.append(f.getOwner());
			}
		}
		return retval.toString();
	}

	/**
	 * @param type
	 *            a terrain type
	 * @return a String representation of that terrain type
	 */
	private static String terrainText(final TileType type) {
		if (DESCRIPTIONS.containsKey(type)) {
			return DESCRIPTIONS.get(type);
		} // else 
		throw new IllegalArgumentException("Unknown terrain type");
	}

	/**
	 * Produce an image representing the given tiletype (should probably use a
	 * static Map?) FIXME: Actually implement.
	 * 
	 * @param type
	 *            the tile type
	 * @return an image representing it
	 */
	public static URL getImageURL(final TileType type) {
		return IMAGES.get(type);
	}

	/**
	 * The tile IMAGES.
	 */
	private static final Map<TileType, URL> IMAGES = new EnumMap<TileType, URL>(
			TileType.class);
	/**
	 * Descriptions of the types.
	 */
	private static final Map<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
	static {
		addImageToMap(TileType.Plains, "plains");
		addImageToMap(TileType.Ocean, "ocean");
		addImageToMap(TileType.TemperateForest, "tforest");
		addImageToMap(TileType.BorealForest, "bforest");
		addImageToMap(TileType.Desert, "desert");
		addImageToMap(TileType.Jungle, "jungle");
		addImageToMap(TileType.Mountain, "mountain");
		addImageToMap(TileType.Tundra, "tundra");
		addImageToMap(TileType.NotVisible, "notvisible");
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

	/**
	 * Fetch and add a tile image to the collection.
	 * 
	 * @param type
	 *            the tile type to associate it with
	 * @param tile
	 *            the unique part of the URL
	 */
	private static void addImageToMap(final TileType type, final String tile) {
		IMAGES.put(type, ViewerFrame.getFrame().getClass().getResource(tile + ".png"));
	}
}
