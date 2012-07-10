package view.map.main;

import static util.EqualsAny.equalsAny;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import model.map.MapView;
import model.map.Tile;
import model.viewer.MapModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import view.util.Coordinate;

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
		setLayout(new BorderLayout());
		setDoubleBuffered(true);
		model = theMap;
		helper = TileDrawHelperFactory.INSTANCE.factory(model.getMainMap().getVersion(), this);
		loadMap(theMap.getMainMap());
		addMouseListener(new ComponentMouseListener(model, this));
		model.addPropertyChangeListener(this);
		new ArrowKeyListener().setUpListeners(
				new DirectionSelectionChanger(model), getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
				getActionMap());
		addComponentListener(new MapSizeListener(model));
	}

	/**
	 * Paint.
	 * 
	 * @param pen
	 *            the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		drawMap(pen);
		super.paint(pen);
	}

	/**
	 * @param pen
	 *            the graphics context
	 */
	private void drawMap(final Graphics pen) {
		final Graphics context = pen.create();
		try {
			context.setColor(Color.white);
			context.fillRect(0, 0, getWidth(), getHeight());
			final Rectangle bounds = bounds(context.getClipBounds());
			final int tsize = TILE_SIZE
					.getSize(model.getMainMap().getVersion());
			drawMapPortion(context, (int) Math.round(bounds.getMinX() / tsize),
					(int) Math.round(bounds.getMinY() / tsize), Math.min(
							(int) Math.round(bounds.getMaxX() / tsize + 1),
							model.getSizeCols()), Math.min(
							(int) Math.round(bounds.getMaxY() / tsize + 1),
							model.getSizeRows()));
		} finally {
			context.dispose();
		}
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
		final int minRow = getMapModel().getDimensions().getMinimumRow();
		final int maxRow = getMapModel().getDimensions().getMaximumRow();
		final int minCol = getMapModel().getDimensions().getMinimumCol(); // NOPMD
		final int maxCol = getMapModel().getDimensions().getMaximumCol(); // NOPMD
		for (int i = minY; i < maxY && i + minRow < maxRow + 1; i++) {
			for (int j = minX; j < maxX && j + minCol < maxCol + 1; j++) {
				paintTile(pen, model.getTile(i + minRow, j + minCol), i, j);
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
		return (rect == null) ? new Rectangle(0, 0, (getMapModel().getDimensions()
				.getMaximumCol() - getMapModel().getDimensions().getMinimumCol())
				* TILE_SIZE.getSize(getMapModel().getMainMap().getVersion()),
				(getMapModel().getDimensions().getMaximumRow() - getMapModel()
						.getDimensions().getMinimumRow()) * TILE_SIZE.getSize(getMapModel().getMainMap().getVersion()))
				: rect;
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
		final int tsize = TILE_SIZE.getSize(getMapModel().getMainMap().getVersion());
		helper.drawTile(pen, tile, new Coordinate(col * tsize, row * tsize), new Coordinate(tsize, tsize));
		if (model.getSelectedTile().equals(tile)) {
			final Graphics context = pen.create();
			try {
			context.setColor(Color.black);
			context.drawRect(col * tsize + 1, row * tsize + 1,
					tsize - 2, tsize - 2);
			} finally {
				context.dispose();
			}
		}
	}

	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	@Override
	public void loadMap(final MapView newMap) {
		model.setMainMap(newMap);
		helper = TileDrawHelperFactory.INSTANCE.factory(newMap.getVersion(), this);
		repaint();
	}

	/**
	 * 
	 * @return the map model
	 */
	@Override
	public MapModel getMapModel() {
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
			repaint();
		}
	}
	/**
	 * @return whether the selected tile is either not in the map or visible in the current bounds.
	 */
	private boolean isSelectionVisible() {
		final int selRow = getMapModel().getSelectedTile().getLocation().row();
		final int selCol = getMapModel().getSelectedTile().getLocation().col();
		final int minRow = getMapModel().getDimensions().getMinimumRow();
		final int maxRow = getMapModel().getDimensions().getMaximumRow();
		final int minCol = getMapModel().getDimensions().getMinimumCol();
		final int maxCol = getMapModel().getDimensions().getMaximumCol();
		return ((selRow <= 0 || selRow >= minRow)
				&& (selRow >= getMapModel().getSizeRows() || selRow <= maxRow)
				&& (selCol <= 0 || selCol >= minCol)
				&& (selCol >= getMapModel().getSizeCols() || selCol <= maxCol));
	}
	/**
	 * Fix the visible dimensions to include the selected tile.
	 */
	private void fixVisibility() {
		final int selRow = Math.max(getMapModel().getSelectedTile().getLocation().row(), 0);
		final int selCol = Math.max(getMapModel().getSelectedTile().getLocation().col(), 0);
		int minRow = getMapModel().getDimensions().getMinimumRow();
		int maxRow = getMapModel().getDimensions().getMaximumRow();
		int minCol = getMapModel().getDimensions().getMinimumCol();
		int maxCol = getMapModel().getDimensions().getMaximumCol();
		if (selRow < minRow) {
			final int diff = minRow - selRow;
			minRow -= diff;
			maxRow -= diff;
		} else if (selRow > maxRow) {
			final int diff = selRow - maxRow;
			minRow += diff;
			maxRow += diff;
		}
		if (selCol < minCol) {
			final int diff = minCol - selCol;
			minCol -= diff;
			maxCol -= diff;
		} else if (selCol > maxCol) {
			final int diff = selCol - maxCol;
			minCol += diff;
			maxCol += diff;
		}
		getMapModel().setDimensions(
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
