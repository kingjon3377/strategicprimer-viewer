package view.user;

import java.net.URL;
import java.util.EnumMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import model.Fortress;
import model.Tile;
import model.TileType;
import model.Unit;

/**
 * A GUI representation of a tile. Information about what's on the tile should
 * be indicated by a small icon or by tooltip text.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class GUITile extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4047750632787337702L;
	/**
	 * The tile this GUI-tile represents
	 */
	private final Tile tile;
	/**
	 * @return the tile this GUI represents.
	 */
	public final Tile getTile() {
		return tile;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param _tile
	 *            the tile this will represent
	 */
	public GUITile(final Tile _tile) {
		super(new ImageIcon(ViewerFrame.getFrame().getToolkit().createImage(
				getImageURL(_tile.getType()))));
		setToolTipText("Terrain: " + terrainText(_tile.getType()) + anyForts(_tile)
				+ anyUnits(_tile));
		tile = _tile;
	}

	/**
	 * @param tile2
	 *            a tile
	 * @return a String representation of the units on the tile, if any,
	 *         preceded by a newline if there are any
	 */
	private static String anyUnits(final Tile tile2) {
		final StringBuffer retval = new StringBuffer("");
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
	private static String anyForts(final Tile tile2) {
		final StringBuffer retval = new StringBuffer();
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
	 * static Map?) 
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
	private static final EnumMap<TileType, URL> IMAGES = new EnumMap<TileType, URL>(
			TileType.class);
	/**
	 * Descriptions of the types.
	 */
	private static final EnumMap<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
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
