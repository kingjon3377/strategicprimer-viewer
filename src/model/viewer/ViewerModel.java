package model.viewer;

import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileType;
import model.misc.AbstractDriverModel;

/**
 * A class to encapsulate the various model-type things views need to do with
 * maps.
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
	 */
	public ViewerModel(final MapView firstMap) {
		setMap(firstMap);
	}

	/**
	 * The currently selected tile in the main map.
	 */
	private Tile selTile;
	/**
	 * @param newMap the new map
	 */
	@Override
	public void setMap(final MapView newMap) {
		super.setMap(newMap);
		// TODO: Perhaps clearSelection() instead of setting to (-1, -1)?
		setSelection(PointFactory.point(-1, -1));
		setDimensions(new VisibleDimensions(0, newMap.getDimensions().rows - 1, 0,
				newMap.getDimensions().cols - 1));
	}
	/**
	 *
	 * @return the currently selected tile
	 */
	@Override
	public Tile getSelectedTile() {
		return selTile;
	}

	/**
	 * Set the new selected tiles, given coordinates.
	 *
	 * @param point the location of the new tile.
	 */
	@Override
	public void setSelection(final Point point) {
		final Tile oldSelection = selTile;
		selTile = getMap().getTile(point);
		firePropertyChange("tile", oldSelection, selTile);
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
		final Tile oldSelection = selTile;
		selTile = new Tile(-1, -1, TileType.NotVisible);
		firePropertyChange("tile", oldSelection, selTile);
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
}
