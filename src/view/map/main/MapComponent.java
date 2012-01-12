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

/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MapComponent extends JComponent implements
		MapGUI, PropertyChangeListener {
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
		loadMap(theMap.getMainMap());
		addMouseListener(new ComponentMouseListener(model, this));
		model.addPropertyChangeListener(this);
		new ArrowKeyListener().setUpListeners(
				new DirectionSelectionChangerImpl(model), getInputMap(),
				getActionMap());
		addComponentListener(new MapSizeListener(model));
	}

	/**
	 * Creates the buffered image.
	 */
	public void createImage() {
		final int tsize = TILE_SIZE.getSize(getModel().getMainMap().getVersion());
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
		final int tsize = TILE_SIZE.getSize(model.getMainMap().getVersion());
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
				* TILE_SIZE.getSize(getModel().getMainMap().getVersion()),
				(getModel().getDimensions().getMaximumRow() - getModel()
						.getDimensions().getMinimumRow()) * TILE_SIZE.getSize(getModel().getMainMap().getVersion()))
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
		final int tsize = TILE_SIZE.getSize(getModel().getMainMap().getVersion());
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
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	@Override
	public void loadMap(final SPMap newMap) {
		model.setMainMap(newMap);
		createImage();
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
		if ("tile".equals(evt.getPropertyName()) && !isSelectionVisible()) {
			fixVisibility();
		} 
		if (equalsAny(evt.getPropertyName(), "map", "tile", "dimensions")) {
			createImage();
			repaint();
		}
	}
	/**
	 * @return whether the selected tile is either not in the map or visible in the current bounds.
	 */
	private boolean isSelectionVisible() {
		final int selRow = getModel().getSelectedTile().getRow();
		final int selCol = getModel().getSelectedTile().getCol();
		final int minRow = getModel().getDimensions().getMinimumRow();
		final int maxRow = getModel().getDimensions().getMaximumRow();
		final int minCol = getModel().getDimensions().getMinimumCol();
		final int maxCol = getModel().getDimensions().getMaximumCol();
		return ((selRow <= 0 || selRow >= minRow)
				&& (selRow >= getModel().getSizeRows() || selRow <= maxRow)
				&& (selCol <= 0 || selCol >= minCol)
				&& (selCol >= getModel().getSizeCols() || selCol <= maxCol));
	}
	/**
	 * Fix the visible dimensions to include the selected tile.
	 */
	private void fixVisibility() {
		final int selRow = getModel().getSelectedTile().getRow();
		final int selCol = getModel().getSelectedTile().getCol();
		int minRow = getModel().getDimensions().getMinimumRow();
		int maxRow = getModel().getDimensions().getMaximumRow();
		int minCol = getModel().getDimensions().getMinimumCol();
		int maxCol = getModel().getDimensions().getMaximumCol();
		while (selRow < minRow) {
			minRow--;
			maxRow--;
		}
		while (selRow > maxRow) {
			minRow++;
			maxRow++;
		}
		while (selCol < minCol) {
			minCol--;
			maxCol--;
		}
		while (selCol > maxCol) {
			minCol++;
			maxCol++;
		}
		getModel().setDimensions(
				new VisibleDimensions(minRow, maxRow, minCol, maxCol));
	}
	/**
	 * @return the size of each tile
	 */
	@Override
	public int getTileSize() {
		return TILE_SIZE.getSize(getModel().getMainMap().getVersion());
	}
}
