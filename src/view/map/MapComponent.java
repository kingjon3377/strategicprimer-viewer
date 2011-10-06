package view.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import model.viewer.SPMap;
import model.viewer.Tile;
import view.map.main.DirectTileDrawHelper;
import view.map.main.MapGUI;
import view.map.main.TileDrawHelper;
import view.map.main.VisibleDimensions;
import view.util.PropertyChangeSource;

/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 * 
 * TODO: implement the selection-management stuff now we no longer need tool-tips.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapComponent extends JComponent implements PropertyChangeSource, MapGUI {
	/**
	 * The map this represents.
	 */
	private SPMap map;
	/**
	 * An image of the map.
	 */
	private transient Image image;
	/**
	 * Tile size.
	 */
	private static final int TILE_SIZE = 13;
	/**
	 * The drawing helper, which does the actual drawing of the tiles.
	 */
	private final TileDrawHelper helper = new DirectTileDrawHelper();
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
		if (image == null) {
			image = new BufferedImage(map.cols() * TILE_SIZE, map.rows() * TILE_SIZE, BufferedImage.TYPE_INT_RGB);
		}
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
		} 
		pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
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
		drawMapPortion(pen, minX, minY, maxX, maxY);
	}

	/**
	 * Draw a subset of the map.
	 * @param pen the graphics context
	 * @param minX the minimum X (row?) to draw
	 * @param minY the minimum Y (col?) to draw
	 * @param maxX the maximum X (row?) to draw
	 * @param maxY the maximum Y (col?) to draw
	 */
	private void drawMapPortion(final Graphics pen, final int minX,
			final int minY, final int maxX, final int maxY) {
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
	private void paintTile(final Graphics pen, final Tile tile,
			final int row, final int col) {
		final Color saveColor = pen.getColor();
			helper.drawTile(pen, tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		pen.setColor(saveColor);
	}

	/**
	 * @return our visible dimensions
	 */
	@Override
	public VisibleDimensions getVisibleDimensions() {
		throw new IllegalStateException("Not implemented yet");
	}
	/**
	 * Load and draw a subset of a map.
	 * 
	 * @param newMap
	 *            the map to load.
	 * @param minRow
	 *            the first row to draw
	 * @param maxRow
	 *            the last row to draw
	 * @param minCol
	 *            the first column to draw
	 * @param maxCol
	 *            the last column to draw
	 */
	@Override
	public void loadMap(final SPMap newMap, final int minRow, final int maxRow,
			final int minCol, final int maxCol) {
		throw new IllegalStateException("Not yet implemented");
	}

	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	@Override
	public void loadMap(final SPMap newMap) {
		map = newMap;
		secondaryMap = new SPMap(map.rows(), map.cols());
		repaint();
	}

	/**
	 * @return the map we represent
	 */
	@Override
	public SPMap getMap() {
		return map;
	}
	/**
	 * The secondary map.
	 */
	private SPMap secondaryMap;
	
	/**
	 * @param secMap
	 *            the new secondary map
	 */
	@Override
	public void setSecondaryMap(final SPMap secMap) {
		secondaryMap = secMap;
	}

	/**
	 * Swap the main and secondary maps, i.e. show the secondary map
	 */
	@Override
	public void swapMaps() {
		final SPMap temp = map;
		loadMap(secondaryMap);
		secondaryMap = temp;
	}

	/**
	 * @return the secondary map
	 */
	@Override
	public SPMap getSecondaryMap() {
		return secondaryMap;
	}

	/**
	 * Copy a tile from the main map to the secondary map.
	 * 
	 * @param selection a tile in the relevant position.
	 */
	@Override
	public void copyTile(final Tile selection) {
		secondaryMap.getTile(selection.getRow(), selection.getCol())
		.update(map.getTile(selection.getRow(),
				selection.getCol()));
	}
}
