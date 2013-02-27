package model.viewer;

import model.map.Point;
import model.map.Tile;
import model.misc.IDriverModel;

/**
 * An interface for a model behind the map viewer, handling the selected tile
 * and visible dimensions, and allowing the caller to get the tile at a specific
 * point.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IViewerModel extends IDriverModel {

	/**
	 *
	 * @return the currently selected tile
	 */
	Tile getSelectedTile();

	/**
	 * Set the new selected tiles, given coordinates.
	 *
	 * @param point the location of the new tile.
	 */
	void setSelection(final Point point);

	/**
	 * @param point a tile's location
	 *
	 * @return the tile at that location
	 */
	Tile getTile(final Point point);

	/**
	 * @param dim the new visible dimensions of the map
	 */
	void setDimensions(final VisibleDimensions dim);

	/**
	 *
	 * @return the visible dimensions of the map
	 */
	VisibleDimensions getDimensions();
	/**
	 * @return the current zoom level
	 */
	int getZoomLevel();
	/**
	 * Zoom in.
	 */
	void zoomIn();
	/**
	 * Zoom out.
	 */
	void zoomOut();
	/**
	 * Reset the zoom level to the default.
	 */
	void resetZoom();
}
