package view.map;

import static util.EqualsAny.equalsAny;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import model.viewer.MapModel;
import model.viewer.SPMap;
import model.viewer.Tile;
import view.map.main.ArrowKeyListener;
import view.map.main.DirectTileDrawHelper;
import view.map.main.DirectionSelectionChangerImpl;
import view.map.main.MapGUI;
import view.map.main.TileDrawHelper;
import view.map.main.VisibleDimensions;
import view.util.PropertyChangeSource;
/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class MapComponent extends JComponent implements PropertyChangeSource,
		MapGUI, PropertyChangeListener {
	/**
	 * The map model encapsulating the map this represents, the secondary map, and the selected tile.
	 */
	private final MapModel model;
	/**
	 * An image of the map.
	 */
	private transient Image image;
	/**
	 * Tile size.
	 */
	private static final int TILE_SIZE = 16;
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
		setDoubleBuffered(true);
		model = new MapModel(theMap);
		loadMap(theMap);
		setMinimumSize(new Dimension(model.getSizeCols() * TILE_SIZE, model.getSizeRows()
				* TILE_SIZE));
		setPreferredSize(getMinimumSize());
		setSize(getMinimumSize());
		addMouseListener(new MouseAdapter() {
			/**
			 * Handle mouse clicks.
			 * 
			 * @param event
			 *            the event to handle
			 */
			@Override
			public void mouseClicked(final MouseEvent event) {
				getModel().setSelection(event.getPoint().y / TILE_SIZE, event.getPoint().x / TILE_SIZE);
				if (event.getClickCount() == 2) {
					firePropertyChange("encounter", "old", "new");
					getModel().copyTile(getModel().getSelectedTile());
				}
			}
		});
		model.addPropertyChangeListener(this);
		new ArrowKeyListener().setUpListeners(new DirectionSelectionChangerImpl(model), getInputMap(), getActionMap());
	}

	/**
	 * Creates the buffered image.
	 */
	public void createImage() {
		image = createImage(
				(getModel().getDimensions().getMaximumCol() + 1 - getModel()
						.getDimensions().getMinimumCol()) * TILE_SIZE,
				(getModel().getDimensions().getMaximumRow() + 1 - getModel()
						.getDimensions().getMinimumRow()) * TILE_SIZE);
		if (image == null) {
			image = new BufferedImage((getModel().getDimensions()
					.getMaximumCol() + 1 - getModel().getDimensions()
					.getMinimumCol())
					* TILE_SIZE,
					(getModel().getDimensions().getMaximumRow() + 1 - getModel()
							.getDimensions().getMinimumRow()) * TILE_SIZE,
					BufferedImage.TYPE_INT_RGB);
		}
		drawMap(image.getGraphics());
		setMinimumSize(new Dimension(
				(getModel().getDimensions().getMaximumCol() - getModel().getDimensions().getMinimumCol() + 1)
						* TILE_SIZE,
				(getModel().getDimensions().getMaximumRow() - getModel().getDimensions()
						.getMinimumRow() + 1) * TILE_SIZE));
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
		pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
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
		final int minX = (int) (bounds.getMinX() / TILE_SIZE);
		final int minY = (int) (bounds.getMinY() / TILE_SIZE);
		final int maxX = Math.min((int) (bounds.getMaxX() / TILE_SIZE + 1),
				model.getSizeCols());
		final int maxY = Math.min((int) (bounds.getMaxY() / TILE_SIZE + 1),
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
		for (int i = minY; i < maxY
				&& i + getModel().getDimensions().getMinimumRow() < getModel().getDimensions()
						.getMaximumRow() + 1; i++) {
			for (int j = minX; j < maxX
					&& j + getModel().getDimensions().getMinimumCol() < getModel().getDimensions()
							.getMaximumCol() + 1; j++) {
				paintTile(
						pen,
						model.getTile(i
								+ getModel().getDimensions().getMinimumRow(), j
								+ getModel().getDimensions().getMinimumCol()), i, j);
			}
		}
	}

	/**
	 * @param rect
	 *            a bounding rectangle
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(final Rectangle rect) {
		return (rect == null) ? new Rectangle(0, 0,
				(getModel().getDimensions().getMaximumCol()
						- getModel().getDimensions().getMinimumCol())
						* TILE_SIZE, (getModel().getDimensions().getMaximumRow()
						- getModel().getDimensions().getMinimumRow())
						* TILE_SIZE) : rect;
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
		final Color saveColor = pen.getColor();
		helper.drawTile(pen, tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE,
				TILE_SIZE);
		if (model.getSelectedTile().equals(tile)) {
			pen.setColor(Color.black);
			pen.drawRect(col * TILE_SIZE + 1, row * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2);
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
		model.setDimensions(new VisibleDimensions(Math.max(0, minRow),
				Math.min(model.getSizeRows(), maxRow + 1) - 1, Math.max(0, minCol),
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
	 * @return the map model
	 */
	@Override
	public MapModel getModel() {
		return model;
	}
	/**
	 * Handle events.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		if (equalsAny(evt.getPropertyName(), "map", "tile", "dimensions")) {
			createImage();
			repaint();
		}
	}
}
