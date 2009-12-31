package view.user;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	 * The size of each GUI tile
	 */
	private static final int TILE_SIZE = 5;
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
		super();
		setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
		try {
			super.setIcon(new ImageIcon(getImage(_tile.getType())));
		} catch (IOException e) {
			Logger.getLogger(GUITile.class.getName()).log(
					Level.SEVERE,
					"I/O error loading image for tile ("
							+ Integer.toString(_tile.getRow()) + ','
							+ Integer.toString(_tile.getCol()) + ')', e);
		}
		setToolTipText("Terrain: " + terrainText(_tile.getType())
				+ anyForts(_tile) + anyUnits(_tile));
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
		return (tile == null || tile.getEvent() == -1 ? "" : "\nEvent: "
				+ Integer.toString(tile.getEvent()));
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
	 * Descriptions of the types.
	 */
	private static final EnumMap<TileType, String> DESCRIPTIONS = new EnumMap<TileType, String>(
			TileType.class);
	/**
	 * A cache of terrain type images
	 */
	private static EnumMap<TileType, Image> imageMap = new EnumMap<TileType, Image>(
			TileType.class);
	/**
	 * A cache of terrain type image filenames
	 */
	private static EnumMap<TileType, String> stringMap = new EnumMap<TileType, String>(
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
	 * Populate the filename cache
	 * 
	 * @param type
	 *            A terrain type
	 * @param tile
	 *            The string to use as the base of the filename
	 */
	private static void addStringToMap(final TileType type, final String tile) {
		stringMap.put(type, '/' + tile + ".png");
	}

	/**
	 * @param terr
	 *            a terrain type
	 * @return an image representing that terrain
	 * @throws IOException
	 *             Thrown by a method used in producing the image
	 */
	private static Image getImage(final TileType terr) throws IOException {
		if (imageMap.containsKey(terr)) {
			return imageMap.get(terr); // NOPMD
		} else {
			final URL url = GUITile.class.getResource(stringMap.get(terr));
			if (url == null) {
				Logger.getLogger(GUITile.class.getName()).severe(
						"Couldn't find image for " + terr);
			}
			return url == null ? null : Toolkit.getDefaultToolkit()
					.createImage((ImageProducer) url.getContent());
		}
	}
}
