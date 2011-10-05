package view.map.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A GUI representation of a tile. Information about what's on the tile should
 * be indicated by a small icon.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class GUITile extends Selectable {
	/**
	 * The helper object to actually do the drawing.
	 */
	private static final TileDrawHelper HELPER = new CachingTileDrawHelper();
	/**
	 * The size of each GUI tile.
	 */
	public static final int TILE_SIZE = 16;
	/**
	 * The size of each GUI tile, as a Dimension.
	 */
	private static final Dimension PREF_SIZE = new Dimension(TILE_SIZE,
			TILE_SIZE);
	/**
	 * The tile this GUI-tile represents.
	 */
	private Tile tile;
	/**
	 * @return the tile this GUI represents.
	 */
	public final Tile getTile() {
		return tile;
	}

	/**
	 * Constructor.
	 * 
	 * @param newTile
	 *            the tile this will represent
	 */
	public GUITile(final Tile newTile) {
		super();
		setPreferredSize(PREF_SIZE);
		setMinimumSize(PREF_SIZE);
		setMaximumSize(null);
		setOpaque(true);
		tile = newTile;
	}

	/**
	 * Cache of what the tile should look like.
	 */
	private BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);

	/**
	 * Check whether that cache is out of date, and recreate it if it is. This
	 * lets us draw everything except the selection box only *once* per tile in
	 * most cases.
	 */
	private void checkImageCache() {
		if (image.getWidth() != getWidth()
				|| image.getHeight() != getHeight()) {
			image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			regenerateCache();
		}
	}

	/**
	 * 
	 */
	private void regenerateCache() {
		final Graphics2D pen = image.createGraphics();
		HELPER.drawTile(pen, tile, image.getWidth(), image.getHeight());
	}
	/**
	 * @param type a tile type
	 * @return the color associated with that tile-type.
	 */
	public static Color getTileColor(final TileType type) {
		return COLORS.get(type);
	}
	/**
	 * Paint the tile.
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		final Color saveColor = pen.getColor();
		checkImageCache();
		pen.drawImage(image, 0, 0, this);
		pen.setColor(saveColor);
	}
	/**
	 * Mapping from tile types to colors.
	 */
	private static final Map<TileType, Color> COLORS = new EnumMap<TileType, Color>(
			TileType.class);
	// ESCA-JAVA0076:
	static {
		COLORS.put(TileType.BorealForest, new Color(72, 218, 164));
		COLORS.put(TileType.Desert, new Color(249, 233, 28));
		COLORS.put(TileType.Jungle, new Color(229, 46, 46));
		COLORS.put(TileType.Mountain, new Color(249, 137, 28));
		COLORS.put(TileType.NotVisible, new Color(255, 255, 255));
		COLORS.put(TileType.Ocean, new Color(0, 0, 255));
		COLORS.put(TileType.Plains, new Color(0, 117, 0));
		COLORS.put(TileType.TemperateForest, new Color(72, 250, 72));
		COLORS.put(TileType.Tundra, new Color(153, 153, 153));
	}

	/**
	 * @param newTile
	 *            the tile this now represents
	 */
	public void setTile(final Tile newTile) {
		tile = newTile;
		regenerateCache();
	}
}
