package view.user;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import model.SPMap;
import model.Tile;
import model.TileType;

/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapComponent extends JComponent {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 435344338279530103L;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapComponent.class
			.getName());
	/**
	 * The map this represents.
	 */
	private final SPMap map;
	/**
	 * An image of the map.
	 */
	private Image image;
	/**
	 * Tile size.
	 */
	private static final int TILE_SIZE = 10;

	/**
	 * Constructor.
	 * 
	 * @param theMap
	 *            The map this represents
	 */
	public MapComponent(final SPMap theMap) {
		super();
		map = theMap;
		setMinimumSize(new Dimension(map.cols() * TILE_SIZE, map.rows()
				* TILE_SIZE));
		setPreferredSize(getMinimumSize());
	}

	/**
	 * Creates the buffered image
	 */
	public void createImage() {
		image = createImage(map.cols() * TILE_SIZE, map.rows() * TILE_SIZE);
		final Graphics pen = image.getGraphics();
		drawMap(pen);
		pen.dispose();
	}

	/**
	 * Paint.
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		if (image == null) {
			drawMap(pen);
			createImage();
		} else {
			pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		}
	}

	/**
	 * @param pen
	 *            the graphics context
	 */
	private final void drawMap(final Graphics pen) {
		final Rectangle bounds = bounds(pen.getClipBounds());
		final int minX = (int) (bounds.getMinX()
				/ TILE_SIZE);
		final int minY = (int) (bounds.getMinY()
				/ TILE_SIZE);
		final int maxX = Math.min((int) (bounds.getMaxX() / TILE_SIZE + 1), map.cols());
		final int maxY = Math.min((int) (bounds.getMaxY() / TILE_SIZE + 1), map.rows());
		for (int i = minY; i < maxY; i++) {
			for (int j = minX; j < maxX; j++) {
				paintTile(pen, map.getTile(i, j), i, j);
			}
		}
	}
	/**
	 * @param rect a bounding rectangle
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(final Rectangle rect) {
		return rect == null ? new Rectangle(0, 0, map.cols() * TILE_SIZE, map.rows() * TILE_SIZE) : rect;
	}
	/**
	 * Paint a tile.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param tile
	 *            the tile to paint
	 * @param row
	 *            which row this is
	 * @param col
	 *            which column this is
	 */
	private void paintTile(final Graphics pen, final Tile tile, final int row,
			final int col) {
		try {
			if (tile.getType().equals(TileType.Plains)) {
					pen.drawImage(getImage(tile.getType()), col * TILE_SIZE, row
							* TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
			} else {
				final Color saveColor = pen.getColor();
				pen.setColor(colorMap.get(tile.getType()));
				pen.fillRect(col * TILE_SIZE, row
						* TILE_SIZE, TILE_SIZE, TILE_SIZE);
				pen.setColor(saveColor);
			}
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O exception in paint()", e);
		}
	}

	/**
	 * @param terr
	 *            a terrain type
	 * @return an image representing that terrain
	 * @throws IOException
	 *             Thrown by a method used in producing the image
	 */
	private static Image getImage(final TileType terr) throws IOException {
		if (!imageMap.containsKey(terr)) {
			imageMap.put(terr, ImageIO.read(GUITile.class.getResource(stringMap
					.get(terr))));
		}
		return imageMap.get(terr);
	}

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
	private static EnumMap<TileType, Color> colorMap = new EnumMap<TileType, Color>(
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
		colorMap.put(TileType.BorealForest, new Color(72,218,164));
		colorMap.put(TileType.Desert, new Color(249,233,28));
		colorMap.put(TileType.Jungle, new Color(229,46,46));
		colorMap.put(TileType.Mountain, new Color(249,137,28));
		colorMap.put(TileType.NotVisible, new Color(255,255,255));
		colorMap.put(TileType.Ocean, new Color(0,0,255));
		colorMap.put(TileType.Plains, new Color(0,117,0));
		colorMap.put(TileType.TemperateForest, new Color(72,250,72));
		colorMap.put(TileType.Tundra, new Color(153,153,153));
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

}
