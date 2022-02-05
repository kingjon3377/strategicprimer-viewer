package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.awt.event.ComponentListener;
import java.awt.Image;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import java.util.Optional;
import static drivers.map_viewer.TileViewSize.scaleZoom;
import java.util.Comparator;
import java.util.function.Predicate;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import drivers.common.MapChangeListener;
import drivers.common.SelectionChangeListener;
import drivers.common.FixtureMatcher;

import common.map.Point;
import common.map.TileFixture;
import common.map.MapDimensions;

import common.map.fixtures.mobile.IUnit;

import java.awt.image.BufferedImage;

/**
 * A component to display the map, even a large one, without the performance
 * problems that came from drawing the entire map every time and letting Java
 * manage the scrolling or, worse, instantiating a GUITile object for every
 * visible tile every time the map was scrolled (or, yet worse again, a GUITile
 * for every tile in the map, and removing them all and adding the visible
 * tiles back in every time the map was scrolled).
 */
/* package */ class MapComponent extends JComponent implements MapGUI, MapChangeListener,
		SelectionChangeListener, GraphicalParamsListener {
	private final IViewerModel mapModel;

	@Override
	public IViewerModel getMapModel() {
		return mapModel;
	}

	private final ComponentMouseListener cml;
	private final DirectionSelectionChanger dsl;
	private TileDrawHelper helper;

	@Nullable
	private BufferedImage backgroundImage = null;

	@Nullable
	public BufferedImage getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(@Nullable final BufferedImage backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public MapComponent(final IViewerModel model, final Predicate<TileFixture> zof,
	                    final Iterable<FixtureMatcher> matchers) { // FIXME: Create an interface extending both Iterable and Comparator, and make FixtureFixtureTableModel, FixtureFilterListModel, etc., implement it, and take it here instead of taking Iterable and casting to Comparator
		mapModel = model;
		cml = new ComponentMouseListener(model, zof, (Comparator<TileFixture>) matchers);
		dsl = new DirectionSelectionChanger(model);
		helper = new Ver2TileDrawHelper(this::imageUpdate, zof, matchers);
		setDoubleBuffered(true);

		addMouseListener(cml);
		addMouseWheelListener(dsl);

		ActionMap localActionMap = getActionMap();
		InputMap localInputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ArrowListenerInitializer.setUpArrowListeners(dsl, localInputMap, localActionMap);
		localInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "show-terrain-menu");
		localActionMap.put("show-terrain-menu", new AbstractAction() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					cml.showMenuAtSelection(Optional.ofNullable(event.getSource())
						.filter(Component.class::isInstance)
						.map(Component.class::cast).orElse(null));
				}
			});
		mapSizeListener = new MapSizeListener(model, this, this::getTileSize);
		addComponentListener(mapSizeListener);

		setToolTipText("");
		addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(final MouseEvent event) {
					repaint();
				}
			});

		setRequestFocusEnabled(true);
	}

	// TODO: cache this?
	private int getTileSize() {
		return scaleZoom(mapModel.getZoomLevel(), mapModel.getMapDimensions().getVersion());
	}

	private Rectangle boundsCheck(@Nullable final Rectangle rect) {
		if (rect != null) { // TODO: invert
			return rect;
		} else {
			VisibleDimensions dimensions = mapModel.getVisibleDimensions();
			return new Rectangle(0, 0, dimensions.getWidth() * getTileSize(),
				dimensions.getHeight() * getTileSize());
		}
	}

	private void fixVisibility() {
		Point selectedPoint = mapModel.getSelection();
		final int selectedRow = Math.max(selectedPoint.getRow(), 0);
		final int selectedColumn = Math.max(selectedPoint.getColumn(), 0);
		VisibleDimensions visibleDimensions = mapModel.getVisibleDimensions();
		int minimumRow = visibleDimensions.getMinimumRow();
		int maximumRow = visibleDimensions.getMaximumRow();
		int minimumColumn = visibleDimensions.getMinimumColumn();
		int maximumColumn = visibleDimensions.getMaximumColumn();
		if (selectedRow < minimumRow) {
			int difference = minimumRow - selectedRow;
			minimumRow -= difference;
			maximumRow -= difference;
		} else if (selectedRow > maximumRow) {
			int difference = selectedRow - maximumRow;
			minimumRow += difference;
			maximumRow += difference;
		}
		if (selectedColumn < minimumColumn) {
			int difference = minimumColumn - selectedColumn;
			minimumColumn -= difference;
			maximumColumn -= difference;
		} else if (selectedColumn > maximumColumn) {
			int difference = selectedColumn - maximumColumn;
			minimumColumn += difference;
			maximumColumn += difference;
		}
		mapModel.setVisibleDimensions(new VisibleDimensions(minimumRow, maximumRow,
			minimumColumn, maximumColumn));
	}

	@Override
	@Nullable
	public String getToolTipText(final MouseEvent event) {
		return cml.getToolTipText(event);
	}

	@Override
	public void dimensionsChanged(final VisibleDimensions oldDim, final VisibleDimensions newDim) {
		repaint();
	}

	private void paintTile(final Graphics pen, final int tileSize, final Point point, final int row, final int column,
	                       final boolean selected) {
		if (!mapModel.getMap().getDimensions().contains(point)) {
			return;
		}
		helper.drawTile(pen, mapModel.getMap(), point,
			new Coordinate(column * tileSize, row * tileSize),
			new Coordinate(tileSize, tileSize));
		if (selected) {
			Graphics context = pen.create();
			try {
				context.setColor(Color.black);
				context.drawRect((column * tileSize) + 1, (row * tileSize) + 1,
					tileSize - 2, tileSize - 2);
			} finally {
				context.dispose();
			}
		}
	}

	private boolean isSelectionVisible() {
		Point selectedPoint = mapModel.getSelection();
		int selectedRow = Math.max(selectedPoint.getRow(), 0);
		int selectedColumn = Math.max(selectedPoint.getColumn(), 0);
		VisibleDimensions visibleDimensions = mapModel.getVisibleDimensions();
		return visibleDimensions.getRows().contains(selectedRow) &&
			visibleDimensions.getColumns().contains(selectedColumn);
	}

	// FIXME: inline into callers; Ceylon couldn't because it's overloaded
	private void requestFocusNarrowly() {
		requestFocusInWindow();
	}

	/**
	 * If the point is currently visible, call the overloading of {@link
	 * repaint} that takes coordinates, passing the coordinates describing
	 * the point.
	 */
	private void repaintPoint(final Point point) {
		VisibleDimensions visibleDimensions = mapModel.getVisibleDimensions();
		int row = Math.max(point.getRow(), 0);
		int column = Math.max(point.getColumn(), 0);
		int tileSize = getTileSize();
		if (visibleDimensions.getRows().contains(row) &&
				visibleDimensions.getColumns().contains(column)) {
			repaint((column - visibleDimensions.getMinimumColumn()) * tileSize,
				(row - visibleDimensions.getMinimumRow()) * tileSize, tileSize, tileSize);
		}
	}

	@Override
	public void selectedPointChanged(@Nullable final Point old, final Point newPoint) {
		SwingUtilities.invokeLater(this::requestFocusInWindow);
		if (isSelectionVisible()) {
			if (old != null && !old.equals(newPoint)) {
				repaintPoint(old);
			}
			repaintPoint(newPoint);
		} else {
			fixVisibility();
			repaint();
		}
	}

	@Override
	public void cursorPointChanged(@Nullable final Point old, final Point newCursor) {} // TODO: check visibility of cursor point here?

	@Override
	public void mapChanged() {}

	private void drawBackgroundImage(final Graphics context, final int tileSize) {
		BufferedImage temp = backgroundImage;
		if (temp != null) { // TODO: invert
			VisibleDimensions visibleDimensions = mapModel.getVisibleDimensions();
			MapDimensions mapDimensions = mapModel.getMapDimensions();
			double horizontalScaling = ((double) temp.getWidth()) / mapDimensions.getColumns();
			double verticalScaling = ((double) temp.getHeight()) / mapDimensions.getRows();
			int x = (int) (visibleDimensions.getMinimumColumn() * horizontalScaling);
			int y = (int) (visibleDimensions.getMinimumRow() * verticalScaling);
			int sliceWidth = (int) (visibleDimensions.getWidth() * horizontalScaling);
			int sliceHeight = (int) (visibleDimensions.getHeight() * verticalScaling);
			// FIXME: Cache this, regenerating when visible dimensions change, somehow
			Image sliced = temp.getSubimage(Math.max(0, x), Math.max(0, y),
				Math.min(temp.getWidth() - x - 1, sliceWidth),
				Math.min(temp.getHeight() - y - 1, sliceHeight));
			context.drawImage(sliced, 0, 0, visibleDimensions.getWidth() * tileSize,
				visibleDimensions.getHeight() * tileSize, null); // TODO: supply observer
		} else {
			context.setColor(Color.white); // TODO: save and restore afterwards?
			context.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	// FIXME: Are these map or screen coordinates? If screen coordinates, what are they relative to?
	private void drawMapPortion(final Graphics context, final int tileSize, final int minX, final int minY,
	                            final int maxX, final int maxY) {
		final int minRow = mapModel.getVisibleDimensions().getMinimumRow();
		final int maxRow = mapModel.getVisibleDimensions().getMaximumRow();
		final int minCol = mapModel.getVisibleDimensions().getMinimumColumn();
		final int maxCol = mapModel.getVisibleDimensions().getMaximumColumn();
		for (int i = minY; i <= maxY; i++) {
			if ((i + minRow) >= (maxRow + 1)) { // TODO: combine with condition in loop
				break;
			}
			for (int j = minX; j <= maxX; j++) {
				if ((j + minCol) >= (maxCol + 1)) { // TODO: combine with condition in loop
					break;
				}
				Point location = new Point(i + minRow, j + minCol);
				paintTile(context, tileSize, location, i, j,
					mapModel.getSelection().equals(location));
			}
		}
	}

	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		Graphics context = pen.create();
		try {
			context.setColor(Color.white);
			context.fillRect(0, 0, getWidth(), getHeight());
			Rectangle bounds = boundsCheck(context.getClipBounds());
			MapDimensions mapDimensions = mapModel.getMapDimensions();
			final int tileSize = getTileSize();
			drawBackgroundImage(context, tileSize);
			// TODO: We used halfEven() around the division
			// operations for rounding in Ceylon; does casting to
			// int do the same? Do we still need the added tenth in Java?
			drawMapPortion(context, tileSize, (int) ((bounds.getMinX() / tileSize) + 0.1),
					(int) ((bounds.getMinY() / tileSize) + 0.1),
				Math.min((int) ((bounds.getMaxX() / tileSize) + 1.1),
					mapDimensions.getColumns()),
				Math.min((int) ((bounds.getMaxY() / tileSize) + 1.1),
					mapDimensions.getRows()));
		} finally {
			context.dispose();
		}
	}

	@Override
	public void interactionPointChanged() {}

	@Override
	public void selectedUnitChanged(@Nullable final IUnit old, @Nullable final IUnit newUnit) {}

	private static class MapSizeListener extends ComponentAdapter {
		private final IViewerModel mapModel;
		private final JComponent outer;
		private final IntSupplier tileSizeFactory;

		public MapSizeListener(final IViewerModel mapModel, final JComponent parent,
		                       final IntSupplier tileSizeFactory) {
			this.mapModel = mapModel;
			outer = parent;
			this.tileSizeFactory = tileSizeFactory;
		}

		// TODO: Split the difference instead of only expanding/contracting on  'max' side
		private Pair<Integer, Integer> constrain(final int total, final int visible, final int oldMinimum) {
			if (visible >= total) {
				return Pair.with(0, total - 1);
			} else if (oldMinimum + visible >= total) {
				return Pair.with(total - visible - 2, total - 1);
			} else {
				return Pair.with(oldMinimum, oldMinimum + visible - 1);
			}
		}

		private Quartet<Integer, Integer, Integer, Integer> concat(final Pair<Integer, Integer> one,
		                                                           final Pair<Integer, Integer> two) {
			return Quartet.with(one.getValue0(), one.getValue1(), two.getValue0(),
				two.getValue1());
		}

		@Override
		public void componentResized(final ComponentEvent event) {
			final int tileSize = tileSizeFactory.getAsInt();
			int visibleColumns = outer.getWidth() / tileSize;
			int visibleRows = outer.getHeight() / tileSize;
			VisibleDimensions oldDimensions = mapModel.getVisibleDimensions();
			MapDimensions mapDimensions = mapModel.getMapDimensions();
			if (visibleColumns != oldDimensions.getWidth() ||
					visibleRows != oldDimensions.getHeight()) {
				Pair<Integer, Integer> constrainedRows =
					constrain(mapDimensions.getRows(), visibleRows,
						oldDimensions.getMinimumRow());
				Pair<Integer, Integer> constrainedCols =
					constrain(mapDimensions.getColumns(), visibleColumns,
						oldDimensions.getMinimumColumn());
				mapModel.setVisibleDimensions(new VisibleDimensions(
					constrainedRows.getValue0(), constrainedRows.getValue1(),
					constrainedCols.getValue0(), constrainedCols.getValue1()));
			}
		}

		@Override
		public void componentShown(final ComponentEvent event) {
			componentResized(event);
		}
	}

	private final MapSizeListener mapSizeListener;

	@Override
	public void tileSizeChanged(final int olSize, final int newSize) {
		ComponentEvent event = new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED);
		for (ComponentListener listener : getComponentListeners()) {
			listener.componentResized(event);
		}
		repaint();
	}

	@Override
	public void mapMetadataChanged() {}
}
