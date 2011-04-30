package view.map.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import model.viewer.River;
import model.viewer.Tile;
import model.viewer.TileType;

/**
 * A GUI representation of a tile. Information about what's on the tile should
 * be indicated by a small icon or by tooltip text.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class GUITile extends Selectable {
	private static final double EIGHT = 8.0;
	/**
	 * The size of each GUI tile
	 */
	private static final int TILE_SIZE = 13;
	/**
	 * The size of each GUI tile, as a Dimension
	 */
	private static final Dimension PREF_SIZE = new Dimension(TILE_SIZE, TILE_SIZE);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4047750632787337702L;
	/**
	 * The tile this GUI-tile represents
	 */
	private final Tile tile;
	/**
	 * A cached copy of our width. If it hasn't changed, we can use cached
	 * rendering objects.
	 */
	// ESCA-JAVA0244:
	private static int width = -1;
	/**
	 * A cached copy of our height. If it hasn't changed, we can used cached
	 * rendering objects.
	 */
	// ESCA-JAVA0244:
	private static int height = -1;
	/**
	 * 7/16
	 */
	private static final double SEVEN_SIXTEENTHS = 7.0 / 16.0;
	/**
	 * A cached copy of our background.
	 */
	private static Rectangle background;
	/**
	 * The shapes representing the rivers on the tile
	 */
	private static final Map<River, Shape> rivers = new EnumMap<River, Shape>( //NOPMD
			River.class);
	/**
	 * Shape representing the fortress that might be on the tile.
	 */
	private static Shape fort;
	/**
	 * Shape representing the unit that might be on the tile
	 */
	private static Shape unit;

	/**
	 * Check, and possibly regenerate, the cache.
	 * 
	 * @param wid
	 *            the current width
	 * @param hei
	 *            the current height
	 */
	private static void checkCache(final int wid, final int hei) {
		if (width != wid || height != hei) {
			width = wid;
			height = hei;
			background = new Rectangle(0, 0, width, height);
			rivers.clear();
			rivers.put(River.East, new Rectangle2D.Double(width / 2.0, height // NOPMD
					* SEVEN_SIXTEENTHS, width / 2.0, height / EIGHT));
			rivers.put(River.Lake, new Ellipse2D.Double(width / 4.0, // NOPMD
					height / 4.0, width / 2.0, height / 2.0));
			rivers.put(River.North, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					0, width / EIGHT, height / 2.0));
			rivers.put(River.South, new Rectangle2D.Double(width
					* SEVEN_SIXTEENTHS, // NOPMD
					height / 2.0, width / EIGHT, height / 2.0));
			rivers.put(River.West, new Rectangle2D.Double(0, height // NOPMD
					* SEVEN_SIXTEENTHS, width / 2.0, height / EIGHT));
			fort = new Rectangle2D.Double(width / 2.0, height / 2.0,
					width / 2.0, height / 2.0);
			unit = new Ellipse2D.Double(width / 4.0, height / 4.0, width / 4.0,
					height / 4.0);
		}
	}

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
		setPreferredSize(PREF_SIZE);
		setMinimumSize(PREF_SIZE);
		setMaximumSize(null);
		setOpaque(true);
		tile = _tile;
	}

	/**
	 * Cache of what the tile should look like.
	 */
	private BufferedImage image;

	/**
	 * Check whether that cache is out of date, and recreate it if it is.
	 * This lets us draw everything except the selection box only *once*
	 * per tile in most cases.
	 */
	private void checkImageCache() {
		if (image == null || image.getWidth() != width || image.getHeight() != height) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			regenerateCache();
		}
	}

	/**
	 * 
	 */
	private void regenerateCache() {
		final Graphics2D pen = image.createGraphics();
		pen.setColor(colorMap.get(tile.getType()));
		pen.fill(background);
		pen.setColor(Color.BLACK);
		pen.draw(background);
		if (!TileType.NotVisible.equals(tile.getType())) {
			pen.setColor(Color.BLUE);
			for (River river : tile.getRivers()) {
				pen.fill(rivers.get(river));
			}
			if (!tile.getForts().isEmpty()) {
				pen.setColor(BROWN);
				pen.fill(fort);
			} 
			if (!tile.getUnits().isEmpty()) {
				pen.setColor(PURPLE);
				pen.fill(unit);
			}
		}
	}

	/**
	 * The identity transformation. drawImage() requires a transformation,
	 * and we *really* don't want to create one every time we paint a tile.
	 */
	private static final AffineTransform IDENT = new AffineTransform();

	/**
	 * Paint the tile
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		// super.paint(pen);
		final Graphics2D pen2d = (Graphics2D) pen;
		final Color saveColor = pen2d.getColor();
		checkCache(getWidth(), getHeight());
		checkImageCache();
		pen2d.drawImage(image, IDENT, this);
		pen2d.setColor(saveColor);
		super.paint(pen);
	}

	private static final Color BROWN = new Color(160, 82, 45);
	private static final Color PURPLE = new Color(148, 0, 211);

	private static EnumMap<TileType, Color> colorMap = new EnumMap<TileType, Color>(
			TileType.class);
	// ESCA-JAVA0076:
	static {
		colorMap.put(TileType.BorealForest, new Color(72, 218, 164));
		colorMap.put(TileType.Desert, new Color(249, 233, 28));
		colorMap.put(TileType.Jungle, new Color(229, 46, 46));
		colorMap.put(TileType.Mountain, new Color(249, 137, 28));
		colorMap.put(TileType.NotVisible, new Color(255, 255, 255));
		colorMap.put(TileType.Ocean, new Color(0, 0, 255));
		colorMap.put(TileType.Plains, new Color(0, 117, 0));
		colorMap.put(TileType.TemperateForest, new Color(72, 250, 72));
		colorMap.put(TileType.Tundra, new Color(153, 153, 153));
	}
}
