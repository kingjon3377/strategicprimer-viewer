package model.viewer;

import model.listeners.GraphicalParamsListener;
import model.listeners.SelectionChangeListener;
import model.listeners.SelectionChangeSupport;
import model.map.IMutableMapNG;
import model.map.Point;
import model.map.PointFactory;
import model.misc.AbstractDriverModel;
import model.misc.IDriverModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A class to encapsulate the various model-type things views need to do with maps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 */
public final class ViewerModel extends AbstractDriverModel implements
		IViewerModel {
	/**
	 * The current zoom level.
	 */
	private int zoomLevel = DEF_ZOOM_LEVEL;
	/**
	 * The starting zoom level.
	 */
	public static final int DEF_ZOOM_LEVEL = 8;
	/**
	 * The maximum zoom level, to make sure that the tile size never overflows.
	 */
	private static final int MAX_ZOOM_LEVEL = Integer.MAX_VALUE / 4;

	/**
	 * The list of graphical-parameter listeners.
	 */
	private final Collection<GraphicalParamsListener> gpListeners = new ArrayList<>();

	/**
	 * The currently selected point in the main map.
	 */
	private Point selPoint;

	/**
	 * The visible dimensions of the map.
	 */
	private VisibleDimensions dimensions;

	/**
	 * The object to handle notifying selection-change listeners.
	 */
	private final SelectionChangeSupport scs = new SelectionChangeSupport();

	/**
	 * Constructor.
	 *
	 * @param firstMap the initial map
	 * @param file     the name the map was loaded from or should be saved to
	 */
	public ViewerModel(final IMutableMapNG firstMap, final File file) {
		dimensions = new VisibleDimensions(0,
				                                  firstMap.dimensions().rows - 1, 0,
				                                  firstMap.dimensions().cols - 1);
		selPoint = PointFactory.point(-1, -1);
		setMap(firstMap, file);
	}

	/**
	 * Copy constructor.
	 *
	 * @param model a driver model
	 */
	public ViewerModel(final IDriverModel model) {
		if (model instanceof IViewerModel) {
			dimensions = ((IViewerModel) model).getDimensions();
			selPoint = ((IViewerModel) model).getSelectedPoint();
		} else {
			dimensions = new VisibleDimensions(0, model.getMapDimensions().rows - 1, 0,
					                                  model.getMapDimensions().cols - 1);
			selPoint = PointFactory.point(-1, -1);
		}
		setMap(model.getMap(), model.getMapFile());
	}

	/**
	 * @param newMap the new map
	 * @param file   the file the map was loaded from or should be saved to
	 */
	@Override
	public void setMap(final IMutableMapNG newMap, final File file) {
		super.setMap(newMap, file);
		clearSelection();
		setDimensions(new VisibleDimensions(0, newMap.dimensions().rows - 1,
				                                   0, newMap.dimensions().cols - 1));
		resetZoom();
	}

	/**
	 * Set the new selected tiles, given coordinates.
	 *
	 * @param point the location of the new tile.
	 */
	@Override
	public void setSelection(final Point point) {
		final Point oldSel = selPoint;
		selPoint = point;
		scs.fireChanges(oldSel, selPoint);
	}

	/**
	 * Clear the selection.
	 */
	public void clearSelection() {
		final Point oldSel = selPoint;
		selPoint = PointFactory.point(-1, -1);
		scs.fireChanges(oldSel, selPoint);
	}

	/**
	 * @param dim the new visible dimensions of the map
	 */
	@Override
	public void setDimensions(final VisibleDimensions dim) {
		for (final GraphicalParamsListener list : gpListeners) {
			list.dimensionsChanged(dimensions, dim);
		}
		dimensions = dim;
	}

	/**
	 * @return the visible dimensions of the map
	 */
	@Override
	public VisibleDimensions getDimensions() {
		return dimensions;
	}

	/**
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "MapModel";
	}

	/**
	 * @return the current zoom level.
	 */
	@Override
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * Zoom in, increasing the zoom level.
	 */
	@Override
	public void zoomIn() {
		if (zoomLevel < MAX_ZOOM_LEVEL) {
			zoomLevel++;
			for (final GraphicalParamsListener list : gpListeners) {
				list.tsizeChanged(zoomLevel - 1, zoomLevel);
			}
		}
	}

	/**
	 * Zoom out, decreasing the zoom level.
	 */
	@Override
	public void zoomOut() {
		if (zoomLevel > 1) {
			zoomLevel--;
			for (final GraphicalParamsListener list : gpListeners) {
				list.tsizeChanged(zoomLevel + 1, zoomLevel);
			}
		}
	}

	/**
	 * Reset the zoom level to the default.
	 */
	@Override
	public void resetZoom() {
		final int old = zoomLevel;
		zoomLevel = DEF_ZOOM_LEVEL;
		for (final GraphicalParamsListener list : gpListeners) {
			list.tsizeChanged(old, zoomLevel);
		}
	}

	/**
	 * @return the location of the currently selected tile
	 */
	@Override
	public Point getSelectedPoint() {
		return selPoint;
	}

	/**
	 * @param list a selection-change listener to add
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		scs.addSelectionChangeListener(list);
	}

	/**
	 * @param list a selection-change listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		scs.removeSelectionChangeListener(list);
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addGraphicalParamsListener(final GraphicalParamsListener list) {
		gpListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeGraphicalParamsListener(final GraphicalParamsListener list) {
		gpListeners.remove(list);
	}
}
