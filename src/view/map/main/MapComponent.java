package view.map.main;

import static util.EqualsAny.equalsAny;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import model.viewer.ZOrderFilter;

/**
 * A component to display the map, even a large one, without the performance
 * problems the previous solutions had. (I hope.)
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapComponent extends JComponent implements MapGUI,
		PropertyChangeListener {
	/**
	 * The map model encapsulating the map this represents, the secondary map,
	 * and the selected tile.
	 */
	private final IViewerModel model;
	/**
	 * The drawing helper, which does the actual drawing of the tiles.
	 */
	private transient TileDrawHelper helper;
	/**
	 * The mouse listener that handles showing the terrain-changing popup menu.
	 */
	private final transient ComponentMouseListener cml;
	/**
	 * The fixture filter (probably a menu).
	 */
	private final transient ZOrderFilter zof;
	/**
	 * Constructor.
	 *
	 * @param theMap The model containing the map this represents
	 * @param zofilt the filter telling which fixtures to draw
	 */
	public MapComponent(final IViewerModel theMap, final ZOrderFilter zofilt) {
		super();
		setDoubleBuffered(true);
		model = theMap;
		zof = zofilt;
		helper = TileDrawHelperFactory.INSTANCE.factory(
				model.getMapDimensions().version, this, zof);
		cml = new ComponentMouseListener(model, this);
		addMouseListener(cml);
		model.addPropertyChangeListener(this);
		final DirectionSelectionChanger dsl = new DirectionSelectionChanger(
				model);
		addMouseWheelListener(dsl);
		requestFocusInWindow();
		ArrowKeyListener.setUpListeners(dsl, getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
				getActionMap());
		addComponentListener(new MapSizeListener(model));
		setToolTipText("");
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(final MouseEvent evt) {
				repaint();
			}
		});
	}
	/**
	 * @param event an event indicating where the mouse is
	 * @return an appropriate tool-tip
	 */
	@Override
	public String getToolTipText(final MouseEvent event) {
		return cml.getToolTipText(event);
	}
	/**
	 * Paint.
	 *
	 * @param pen the graphics context
	 */
	@Override
	public void paint(final Graphics pen) {
		drawMap(pen);
		super.paint(pen);
	}

	/**
	 * @param pen the graphics context
	 */
	private void drawMap(final Graphics pen) {
		final Graphics context = pen.create();
		try {
			context.setColor(Color.white);
			context.fillRect(0, 0, getWidth(), getHeight());
			final Rectangle bounds = bounds(context.getClipBounds());
			final MapDimensions mapDim = model.getMapDimensions();
			final int tsize = TileViewSize.scaleZoom(model.getZoomLevel(),
					mapDim.getVersion());
			drawMapPortion(context, (int) Math.round(bounds.getMinX() / tsize),
					(int) Math.round(bounds.getMinY() / tsize), Math.min(
							(int) Math.round(bounds.getMaxX() / tsize + 1),
							mapDim.cols), Math.min(
							(int) Math.round(bounds.getMaxY() / tsize + 1),
							mapDim.rows));
		} finally {
			context.dispose();
		}
	}

	/**
	 * Draw a subset of the map.
	 *
	 * @param pen the graphics context
	 * @param minX the minimum X (row?) to draw
	 * @param minY the minimum Y (col?) to draw
	 * @param maxX the maximum X (row?) to draw
	 * @param maxY the maximum Y (col?) to draw
	 */
	private void drawMapPortion(final Graphics pen, final int minX,
			final int minY, final int maxX, final int maxY) {
		final int minRow = getMapModel().getDimensions().getMinimumRow();
		final int maxRow = getMapModel().getDimensions().getMaximumRow();
		final int minCol = getMapModel().getDimensions().getMinimumCol(); // NOPMD
		final int maxCol = getMapModel().getDimensions().getMaximumCol(); // NOPMD
		for (int i = minY; i < maxY && i + minRow < maxRow + 1; i++) {
			for (int j = minX; j < maxX && j + minCol < maxCol + 1; j++) {
				final Point location = PointFactory.point(i + minRow, j + minCol);
				paintTile(pen, model.getTile(location), i, j, getMapModel()
						.getSelectedPoint().equals(location));
			}
		}
	}

	/**
	 * @param rect a bounding rectangle
	 *
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(final Rectangle rect) {
		final int tsize = TileViewSize.scaleZoom(getMapModel().getZoomLevel(),
				getMapModel().getMapDimensions().getVersion());
		final VisibleDimensions dim = getMapModel().getDimensions();
		return (rect == null) ? new Rectangle(0, 0,
				(dim.getMaximumCol() - dim.getMinimumCol()) * tsize,
				(dim.getMaximumRow() - dim.getMinimumRow()) * tsize) : rect;
	}

	/**
	 * Paint a tile.
	 *
	 * @param pen the graphics context
	 * @param tile the tile to paint
	 * @param row which row this is
	 * @param col which column this is
	 * @param selected whether the tile is the selected tile
	 */
	private void paintTile(final Graphics pen, final Tile tile, final int row,
			final int col, final boolean selected) {
		final int tsize = TileViewSize.scaleZoom(getMapModel().getZoomLevel(),
				getMapModel().getMapDimensions().getVersion());
		helper.drawTile(pen, tile, PointFactory.coordinate(col * tsize, row * tsize),
				PointFactory.coordinate(tsize, tsize));
		if (selected) {
			final Graphics context = pen.create();
			try {
				context.setColor(Color.black);
				context.drawRect(col * tsize + 1, row * tsize + 1, tsize - 2,
						tsize - 2);
			} finally {
				context.dispose();
			}
		}
	}
	/**
	 *
	 * @return the map model
	 */
	@Override
	public IViewerModel getMapModel() {
		return model;
	}

	/**
	 * Handle events.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
				evt.getNewValue());
		if (equalsAny(evt.getPropertyName(), "tile", "point") && !isSelectionVisible()) {
			fixVisibility();
		} else if ("map".equals(evt.getPropertyName())) {
			helper = TileDrawHelperFactory.INSTANCE.factory(
					model.getMapDimensions().version, this, zof);
		} else if ("tsize".equals(evt.getPropertyName())) {
			final ComponentEvent resizeEvt = new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED);
			for (final ComponentListener list : getComponentListeners()) {
				list.componentResized(resizeEvt);
			}
		}
		if (equalsAny(evt.getPropertyName(), "map", "point", "tile", "dimensions", "tsize")) {
			repaint();
		}
	}

	/**
	 * @return whether the selected tile is either not in the map or visible in
	 *         the current bounds.
	 */
	private boolean isSelectionVisible() {
		final int selRow = getMapModel().getSelectedPoint().row;
		final int selCol = getMapModel().getSelectedPoint().col;
		final int minRow = getMapModel().getDimensions().getMinimumRow();
		final int maxRow = getMapModel().getDimensions().getMaximumRow();
		final int minCol = getMapModel().getDimensions().getMinimumCol();
		final int maxCol = getMapModel().getDimensions().getMaximumCol();
		final MapDimensions mapDim = getMapModel().getMapDimensions();
		return (selRow <= 0 || selRow >= minRow)
				&& (selRow >= mapDim.rows || selRow <= maxRow)
				&& (selCol <= 0 || selCol >= minCol) && (selCol >= mapDim.cols || selCol <= maxCol);
	}

	/**
	 * Fix the visible dimensions to include the selected tile.
	 */
	private void fixVisibility() {
		final int selRow = Math.max(getMapModel().getSelectedPoint().row, 0);
		final int selCol = Math.max(getMapModel().getSelectedPoint().col, 0);
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
}
