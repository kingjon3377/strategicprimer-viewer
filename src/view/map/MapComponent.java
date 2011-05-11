package view.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JComponent;

import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;

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
	 * The map this represents.
	 */
	private final SPMap map;
	/**
	 * An image of the map.
	 */
	private transient Image image;
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
	 * Creates the buffered image.
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
	private void drawMap(final Graphics pen) {
		final Rectangle bounds = bounds(pen.getClipBounds());
		final int minX = (int) (bounds.getMinX() / TILE_SIZE);
		final int minY = (int) (bounds.getMinY() / TILE_SIZE);
		final int maxX = Math.min((int) (bounds.getMaxX() / TILE_SIZE + 1),
				map.cols());
		final int maxY = Math.min((int) (bounds.getMaxY() / TILE_SIZE + 1),
				map.rows());
		for (int i = minY; i < maxY; i++) {
			for (int j = minX; j < maxX; j++) {
				paintTile(pen, map.getTile(i, j), i, j);
			}
		}
	}

	/**
	 * @param rect
	 *            a bounding rectangle
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(final Rectangle rect) {
		return (rect == null) ? new Rectangle(0, 0, map.cols() * TILE_SIZE,
				map.rows() * TILE_SIZE) : rect;
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
	private static void paintTile(final Graphics pen, final Tile tile,
			final int row, final int col) {
		final Color saveColor = pen.getColor();
		pen.setColor(COLORS.get(tile.getType()));
		pen.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		pen.setColor(Color.BLACK);
		pen.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		if (tile.getForts().isEmpty()
				|| tile.getType().equals(TileType.NotVisible)) {
			if (!tile.getUnits().isEmpty()
					&& !tile.getType().equals(TileType.NotVisible)) {
				pen.setColor(UNIT_COLOR);
				pen.fillOval(col * TILE_SIZE + TILE_SIZE / 2, row * TILE_SIZE
						+ TILE_SIZE / 2, TILE_SIZE / 4, TILE_SIZE / 4);
			}
		} else {
			pen.setColor(FORT_COLOR);
			pen.fillRect(col * TILE_SIZE + TILE_SIZE / 4, row * TILE_SIZE
					+ TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);
		}
		pen.setColor(saveColor);
	}
	/**
	 * Brown, the color of a fortress.
	 */
	private static final Color FORT_COLOR = new Color(160, 82, 45);
	/**
	 * Purple, the color of a unit.
	 */
	private static final Color UNIT_COLOR = new Color(148, 0, 211);
	/**
	 * Map from tile types to colors representing them.
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

}
