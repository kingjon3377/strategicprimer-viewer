package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import model.listeners.GraphicalParamsListener;
import model.listeners.MapChangeListener;
import model.listeners.SelectionChangeListener;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.viewer.FixtureFilterTableModel;
import model.viewer.FixtureMatcher;
import model.viewer.IViewerModel;
import model.viewer.TileViewSize;
import model.viewer.VisibleDimensions;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A component to display the map, even a large one, without the performance problems the
 * previous solutions had. (I hope.)
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapComponent extends JComponent
		implements MapGUI, MapChangeListener, SelectionChangeListener,
						   GraphicalParamsListener {
	/**
	 * The map model encapsulating the map this represents, the secondary map, and the
	 * selected tile.
	 */
	private final IViewerModel model;
	/**
	 * The mouse listener that handles showing the terrain-changing popup menu.
	 */
	private final ComponentMouseListener cml;
	/**
	 * The fixture filter (probably a menu).
	 */
	private final ZOrderFilter zof;
	/**
	 * The matchers to tell the order in which to draw fixtures.
	 */
	private final Iterable<FixtureMatcher> matchers;
	/**
	 * The drawing helper, which does the actual drawing of the tiles.
	 */
	private TileDrawHelper helper;

	/**
	 * Constructor.
	 *
	 * @param theMap     The model containing the map this represents
	 * @param filter     the filter telling which fixtures to draw
	 * @param fixOrderer a stream of matchers to say which fixture is "on top"
	 */
	public MapComponent(final IViewerModel theMap, final ZOrderFilter filter,
						final FixtureFilterTableModel fixOrderer) {
		setDoubleBuffered(true);
		model = theMap;
		zof = filter;
		matchers = fixOrderer;
		//noinspection TrivialMethodReference
		helper = TileDrawHelperFactory.INSTANCE.factory(
				model.getMapDimensions().version, this::imageUpdate, zof, matchers);
		cml = new ComponentMouseListener(model, zof, fixOrderer);
		//noinspection TrivialMethodReference
		cml.addSelectionChangeListener(this::selectedPointChanged);
		addMouseListener(cml);
		final DirectionSelectionChanger dsl = new DirectionSelectionChanger(model);
		addMouseWheelListener(dsl);
		requestFocusInWindow();
		final ActionMap actionMap = getActionMap();
		if (actionMap == null) {
			throw new IllegalStateException("Action map was null");
		}
		final InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		if (inputMap == null) {
			throw new IllegalStateException("Input map was null");
		}
		ArrowKeyListener.setUpListeners(dsl, inputMap, actionMap);
		addComponentListener(new MapSizeListener());
		setToolTipText("");
		addMouseMotionListener(new MouseMotionAdapter() {
			@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
			@Override
			public void mouseMoved(@Nullable final MouseEvent evt) {
				repaint();
			}
		});
		setRequestFocusEnabled(true);
	}

	/**
	 * The tool-tip text to show on mouse-hover.
	 * @param event an event indicating where the mouse is
	 * @return an appropriate tool-tip
	 */
	@Override
	@Nullable
	public String getToolTipText(@Nullable final MouseEvent event) {
		if (event == null) {
			return null;
		} else {
			return cml.getToolTipText(event);
		}
	}

	/**
	 * Paint.
	 *
	 * @param pen the graphics context
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void paint(@Nullable final Graphics pen) {
		if (pen == null) {
			throw new IllegalStateException("Given a null Graphics");
		}
		drawMap(pen);
		super.paint(pen);
	}

	/**
	 * Paint the map.
	 * @param pen the graphics context
	 */
	private void drawMap(final Graphics pen) {
		final Graphics context = pen.create();
		try {
			context.setColor(Color.white);
			context.fillRect(0, 0, getWidth(), getHeight());
			final Rectangle bounds = bounds(context.getClipBounds());
			final MapDimensions mapDim = model.getMapDimensions();
			final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
					mapDim.getVersion());
			drawMapPortion(context, (int) Math.round(bounds.getMinX() / tileSize),
					(int) Math.round(bounds.getMinY() / tileSize), Math.min(
							(int) Math.round((bounds.getMaxX() / tileSize) + 1),
							mapDim.cols), Math.min(
							(int) Math.round((bounds.getMaxY() / tileSize) + 1),
							mapDim.rows));
		} finally {
			context.dispose();
		}
	}

	/**
	 * Draw a subset of the map.
	 *
	 * @param pen  the graphics context
	 * @param minX the minimum X (row?) to draw
	 * @param minY the minimum Y (col?) to draw
	 * @param maxX the maximum X (row?) to draw
	 * @param maxY the maximum Y (col?) to draw
	 */
	private void drawMapPortion(final Graphics pen, final int minX,
								final int minY, final int maxX, final int maxY) {
		final int minRow = model.getDimensions().getMinimumRow();
		final int maxRow = model.getDimensions().getMaximumRow();
		final int minCol = model.getDimensions().getMinimumCol();
		final int maxCol = model.getDimensions().getMaximumCol();
		for (int i = minY; (i < maxY) && ((i + minRow) < (maxRow + 1)); i++) {
			for (int j = minX; (j < maxX) && ((j + minCol) < (maxCol + 1)); j++) {
				final Point location = PointFactory.point(i + minRow, j + minCol);
				paintTile(pen, location, i, j,
						model.getSelectedPoint().equals(location));
			}
		}
	}

	/**
	 * If given a rectangle, return it; otherwise, return a rectangle surrounding the
	 * whole map.
	 * @param rect a bounding rectangle
	 * @return it, or a rectangle surrounding the whole map if it's null
	 */
	private Rectangle bounds(@Nullable final Rectangle rect) {
		if (rect == null) {
			final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
					model.getMapDimensions().getVersion());
			final VisibleDimensions dim = model.getDimensions();
			return new Rectangle(0, 0, (dim.getMaximumCol() - dim.getMinimumCol()) *
											   tileSize,
										(dim.getMaximumRow() - dim.getMinimumRow()) *
												tileSize);
		} else {
			return rect;
		}
	}

	/**
	 * Paint a tile.
	 *
	 * @param pen      the graphics context
	 * @param point    the point being drawn
	 * @param row      which row this is
	 * @param col      which column this is
	 * @param selected whether the tile is the selected tile
	 */
	private void paintTile(final Graphics pen, final Point point, final int row,
						   final int col, final boolean selected) {
		final int tileSize = TileViewSize.scaleZoom(model.getZoomLevel(),
				model.getMapDimensions().getVersion());
		helper.drawTile(pen, model.getMap(), point,
				PointFactory.coordinate(col * tileSize, row * tileSize),
				PointFactory.coordinate(tileSize, tileSize));
		if (selected) {
			final Graphics context = pen.create();
			try {
				context.setColor(Color.black);
				context.drawRect((col * tileSize) + 1, (row * tileSize) + 1, tileSize
																					 - 2,
						tileSize - 2);
			} finally {
				context.dispose();
			}
		}
	}

	/**
	 * The driver model we represent.
	 * @return the map model
	 */
	@Override
	public IViewerModel getMapModel() {
		return model;
	}

	/**
	 * Handle a change in the map's visible dimensions.
	 * @param oldDim the old visible dimensions
	 * @param newDim the new visible dimensions
	 */
	@Override
	public void dimensionsChanged(final VisibleDimensions oldDim,
								  final VisibleDimensions newDim) {
		repaint();
	}

	/**
	 * Handle a change in the zoom level.
	 * @param oldSize the old zoom level
	 * @param newSize the new zoom level
	 */
	@Override
	public void tileSizeChanged(final int oldSize, final int newSize) {
		final ComponentEvent evt = new ComponentEvent(this,
															 ComponentEvent
																	 .COMPONENT_RESIZED);
		for (final ComponentListener list : getComponentListeners()) {
			list.componentResized(evt);
		}
		repaint();
	}

	/**
	 * Handle a change in the selected point.
	 * @param old      ignored
	 * @param newPoint ignored
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
									 final Point newPoint) {
		SwingUtilities.invokeLater(this::requestFocusInWindow);
		if (!isSelectionVisible()) {
			fixVisibility();
		}
		repaint();
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		helper = TileDrawHelperFactory.INSTANCE.factory(
				model.getMapDimensions().version, this, zof, matchers);
		repaint();
	}

	/**
	 * Whether the selected tile is, if in the map at all, currently visible.
	 * @return whether the selected tile is either not in the map or visible in the
	 * current bounds.
	 */
	private boolean isSelectionVisible() {
		final int selRow = model.getSelectedPoint().getRow();
		final int selCol = model.getSelectedPoint().getCol();
		final int minRow = model.getDimensions().getMinimumRow();
		final int maxRow = model.getDimensions().getMaximumRow();
		final int minCol = model.getDimensions().getMinimumCol();
		final int maxCol = model.getDimensions().getMaximumCol();
		final MapDimensions mapDim = model.getMapDimensions();
		return ((selRow < 0) || (selRow >= minRow))
					   && ((selRow >= mapDim.rows) || (selRow <= maxRow))
					   && ((selCol < 0) || (selCol >= minCol))
					   && ((selCol >= mapDim.cols) || (selCol <= maxCol));
	}

	/**
	 * Fix the visible dimensions to include the selected tile.
	 */
	private void fixVisibility() {
		final int selRow = Math.max(model.getSelectedPoint().getRow(), 0);
		final int selCol = Math.max(model.getSelectedPoint().getCol(), 0);
		int minRow = model.getDimensions().getMinimumRow();
		int maxRow = model.getDimensions().getMaximumRow();
		int minCol = model.getDimensions().getMinimumCol();
		int maxCol = model.getDimensions().getMaximumCol();
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
		model.setDimensions(
				new VisibleDimensions(minRow, maxRow, minCol, maxCol));
	}

	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * A simple toString().
	 * @return a quasi-diagnostic String
	 */
	@Override
	public String toString() {
		return "MapComponent depicting a map of version " +
					   model.getMapDimensions().version;
	}
}
