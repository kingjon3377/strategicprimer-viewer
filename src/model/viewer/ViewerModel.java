package model.viewer;

import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.misc.AbstractDriverModel;

/**
 * A class to encapsulate the various model-type things views need to do with
 * maps.
 *
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public final class ViewerModel extends AbstractDriverModel implements IViewerModel {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param firstMap the initial map
	 * @param filename the name the map was loaded from or should be saved to
	 */
	public ViewerModel(final MapView firstMap, final String filename) {
		setMap(firstMap, filename);
	}

	/**
	 * The currently selected point in the main map.
	 */
	private Point selPoint;
	/**
	 * @param newMap the new map
	 * @param name the filename the map was loaded from or should be saved to
	 */
	@Override
	public void setMap(final MapView newMap, final String name) {
		super.setMap(newMap, name);
		clearSelection();
		setDimensions(new VisibleDimensions(0, newMap.getDimensions().rows - 1, 0,
				newMap.getDimensions().cols - 1));
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
		firePropertyChange("point", oldSel, selPoint);
		firePropertyChange("tile", getMap().getTile(oldSel), getMap().getTile(selPoint));
	}
	/**
	 * @param point a tile's location
	 *
	 * @return the tile at that location
	 */
	@Override
	public Tile getTile(final Point point) {
		return getMap().getTile(point);
	}

	/**
	 * Clear the selection.
	 */
	public void clearSelection() {
		final Point oldSel = selPoint;
		selPoint = PointFactory.point(-1, -1);
		firePropertyChange("point", oldSel, selPoint);
		firePropertyChange("tile", getMap().getTile(oldSel), getMap().getTile(selPoint));
	}

	/**
	 * The visible dimensions of the map.
	 */
	private VisibleDimensions dimensions;

	/**
	 * @param dim the new visible dimensions of the map
	 */
	@Override
	public void setDimensions(final VisibleDimensions dim) {
		firePropertyChange("dimensions", dimensions, dim);
		dimensions = dim;
	}

	/**
	 *
	 * @return the visible dimensions of the map
	 */
	@Override
	public VisibleDimensions getDimensions() {
		return dimensions;
	}

	/**
	 *
	 * @return a String representation of the class
	 */
	@Override
	public String toString() {
		return "MapModel";
	}
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
			firePropertyChange("tsize", Integer.valueOf(zoomLevel - 1),
					Integer.valueOf(zoomLevel));
		}
	}
	/**
	 * Zoom out, decreasing the zoom level.
	 */
	@Override
	public void zoomOut() {
		if (zoomLevel > 1) {
			zoomLevel--;
			firePropertyChange("tsize", Integer.valueOf(zoomLevel + 1),
					Integer.valueOf(zoomLevel));
		}
	}
	/**
	 * Reset the zoom level to the default.
	 */
	@Override
	public void resetZoom() {
		final int old = zoomLevel;
		zoomLevel = DEF_ZOOM_LEVEL;
		firePropertyChange("tsize", Integer.valueOf(old),
				Integer.valueOf(zoomLevel));
	}
	/**
	 * @return the location of the currently selected tile
	 */
	@Override
	public Point getSelectedPoint() {
		return selPoint;
	}
}
