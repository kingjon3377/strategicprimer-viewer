package view.map.main;

import static util.EqualsAny.equalsAny;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import model.map.SPMap;
import model.map.Tile;
import model.viewer.MapModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import util.PropertyChangeSource;

/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MapComponent extends JComponent implements
		PropertyChangeSource, MapGUI, PropertyChangeListener {
	/**
	 * The map model encapsulating the map this represents, the secondary map,
	 * and the selected tile.
	 */
	private final MapModel model;
	/**
	 * An image of the map.
	 */
	private transient Image image;

	/**
	 * 
	 * @return the size of a visible tile.
	 */
	public static TileViewSize getTileSize() {
		return TILE_SIZE;
	}

	/**
	 * Tile size.
	 */
	private static final TileViewSize TILE_SIZE = new TileViewSize();
	/**
	 * The drawing helper, which does the actual drawing of the tiles.
	 */
	private TileDrawHelper helper;

	/**
	 * Constructor.
	 * 
	 * @param theMap
	 *            The model containing the map this represents
	 */
	public MapComponent(final MapModel theMap) {
		super();
		setDoubleBuffered(true);
		if (theMap.getMainMap().getVersion() == 1) {
			helper = new DirectTileDrawHelper(); 
		} else if (theMap.getMainMap().getVersion() == 2) {
			helper = new Ver2TileDrawHelper(this);
		}
		model = theMap;
		final int tsize = getTileSize().getSize(model.getMainMap().getVersion());
		loadMap(theMap.getMainMap());
		setMinimumSize(new Dimension(model.getSizeCols() * tsize,
				model.getSizeRows() * tsize));
		setPreferredSize(getMinimumSize());
		setSize(getMinimumSize());
		addMouseListener(new ComponentMouseListener(model, this));
		model.addPropertyChangeListener(this);
		new ArrowKeyListener().setUpListeners(
				new DirectionSelectionChangerImpl(model), getInputMap(),
				getActionMap());
	}

	/**
	 * Creates the buffered image.
	 */
	public void createImage() {
		final int tsize = getTileSize().getSize(getModel().getMainMap().getVersion());
		image = createImage(
				(getModel().getDimensions().getMaximumCol() + 1 - getModel()
						.getDimensions().getMinimumCol()) * tsize,
				(getModel().getDimensions().getMaximumRow() + 1 - getModel()
						.getDimensions().getMinimumRow()) * tsize);
		if (image == null) {
			image = new BufferedImage((getModel().getDimensions()
					.getMaximumCol() + 1 - getModel().getDimensions()
					.getMinimumCol())
					* tsize, (getModel().getDimensions()
					.getMaximumRow() + 1 - getModel().getDimensions()
					.getMinimumRow())
					* tsize, BufferedImage.TYPE_INT_RGB);
		}
		drawMap(image.getGraphics());
		setMinimumSize(new Dimension(
				(getModel().getDimensions().getMaximumCol()
						- getModel().getDimensions().getMinimumCol() + 1)
						* tsize, (getModel().getDimensions()
						.getMaximumRow()
						- getModel().getDimensions().getMinimumRow() + 1)
						* tsize));
		setPreferredSize(getMinimumSize());
		setSize(getMinimumSize());
		revalidate();
	}

	/**
	 * Paint.
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		if (image == null) {
			drawMap(pen);
			createImage();
		}
		pen.drawImage(image, 0, 0, this);
	}

	/**
	 * @param pen
	 *            the graphics context
	 */
	private void drawMap(final Graphics pen) {
		final Color save = pen.getColor();
		pen.setColor(Color.white);
		pen.fillRect(0, 0, getWidth(), getHeight());
		final Rectangle bounds = bounds(pen.getClipBounds());
		final int tsize = getTileSize().getSize(model.getMainMap().getVersion());
		final int minX = (int) (bounds.getMinX() / tsize);
		final int minY = (int) (bounds.getMinY() / tsize);
		final int maxX = Math.min((int) (bounds.getMaxX() / tsize + 1),
				model.getSizeCols());
		final int maxY = Math.min((int) (bounds.getMaxY() / tsize + 1),
				model.getSizeRows());
		drawMapPortion(pen, minX, minY, maxX, maxY);
		pen.setColor(save);
	}

	/**
	 * Draw a subset of the map.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param minX
	 *            the minimum X (row?) to draw
	 * @param minY
	 *            the minimum Y (col?) to draw
	 * @param maxX
	 *            the maximum X (row?) to draw
	 * @param maxY
	 *            the maximum Y (col?) to draw
	 */
	private void drawMapPortion(final Graphics pen, final int minX,
			final int minY, final int maxX, final int maxY) {
		final int minRow = getModel().getDimensions().getMinimumRow();
		final int maxRow = getModel().getDimensions().getMaximumRow();
		final int minCol = getModel().getDimensions().getMinimumCol(); // NOPMD
		final int maxCol = getModel().getDimensions().getMaximumCol(); // NOPMD
		for (int i = minY; i < maxY && i + minRow < maxRow + 1; i++) {
			for (int j = minX; j < maxX && j + minCol < maxCol + 1; j++) {
				paintTile(pen, model.getMainMap().getVersion(), model.getTile(i + minRow, j + minCol), i, j);
			}
		}
	}

	/**
	 * @param rect
	 *            a bounding rectangle
	 * 
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(final Rectangle rect) {
		return (rect == null) ? new Rectangle(0, 0, (getModel().getDimensions()
				.getMaximumCol() - getModel().getDimensions().getMinimumCol())
				* getTileSize().getSize(getModel().getMainMap().getVersion()),
				(getModel().getDimensions().getMaximumRow() - getModel()
						.getDimensions().getMinimumRow()) * getTileSize().getSize(getModel().getMainMap().getVersion()))
				: rect;
	}

	/**
	 * Paint a tile.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param version the map version
	 * @param tile
	 *            the tile to paint
	 * @param row
	 *            which row this is
	 * @param col
	 *            which column this is
	 */
	private void paintTile(final Graphics pen, final int version, final Tile tile, final int row,
			final int col) {
		final Color saveColor = pen.getColor();
		final int tsize = getTileSize().getSize(getModel().getMainMap().getVersion());
		helper.drawTile(pen, version, tile, col * tsize, row * tsize,
				tsize, tsize);
		if (model.getSelectedTile().equals(tile)) {
			pen.setColor(Color.black);
			pen.drawRect(col * tsize + 1, row * tsize + 1,
					tsize - 2, tsize - 2);
		}
		pen.setColor(saveColor);
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
		model.setMainMap(newMap);
		model.setDimensions(new VisibleDimensions(Math.max(0, minRow), Math
				.min(model.getSizeRows(), maxRow + 1) - 1, Math.max(0, minCol),
				Math.min(model.getSizeCols(), maxCol + 1) - 1));
		createImage();
	}

	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	@Override
	public void loadMap(final SPMap newMap) {
		loadMap(newMap, 0, newMap.rows() - 1, 0, newMap.cols() - 1);
	}

	/**
	 * 
	 * @return the map model
	 */
	@Override
	public MapModel getModel() {
		return model;
	}

	/**
	 * Handle events.
	 * 
	 * @param evt
	 *            the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
				evt.getNewValue());
		if (equalsAny(evt.getPropertyName(), "map", "tile", "dimensions")) {
			createImage();
			repaint();
		}
	}
}
