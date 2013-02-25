package model.viewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import model.map.MapDimensions;
import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileType;

/**
 * A class to encapsulate the various model-type things views need to do with
 * maps.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapModel implements IViewerModel {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param firstMap the initial map
	 */
	public MapModel(final MapView firstMap) {
		setMap(firstMap);
	}

	/**
	 * A helper object to handle property-change listeners for us.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Add a property-change listener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}

	/**
	 * Remove a property-change listener.
	 *
	 * @param list the listener to remove.
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}

	/**
	 * The main map.
	 */
	private MapView map;
	/**
	 * The dimensions of the map.
	 */
	private MapDimensions mapDim;
	/**
	 * The currently selected tile in the main map.
	 */
	private Tile selTile;
	/**
	 * @param newMap the new map
	 */
	@Override
	public void setMap(final MapView newMap) {
		if (mapDim == null) {
			pcs.firePropertyChange("version", -1, newMap.getDimensions()
					.getVersion());
		} else {
			pcs.firePropertyChange("version", mapDim.version,
					newMap.getDimensions().version);
		}
		map = newMap;
		mapDim = newMap.getDimensions();
		// TODO: Perhaps clearSelection() instead of setting to (-1, -1)?
		setSelection(PointFactory.point(-1, -1));
		setDimensions(new VisibleDimensions(0, getMapDimensions().rows - 1, 0,
				getMapDimensions().cols - 1));
		pcs.firePropertyChange("map", map, newMap);
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
		selTile = map.getTile(point);
		pcs.firePropertyChange("tile", oldSelection, selTile);
	}
	/**
	 * @return the dimensions and version of the map
	 */
	public MapDimensions getMapDimensions() {
		return mapDim;
	}
	/**
	 * @param point a tile's location
	 *
	 * @return the tile at that location
	 */
	@Override
	public Tile getTile(final Point point) {
		return map.getTile(point);
	}

	/**
	 *
	 * @return the main map
	 */
	@Override
	public MapView getMap() {
		return map;
	}
	/**
	 * Clear the selection.
	 */
	public void clearSelection() {
		final Tile oldSelection = selTile;
		selTile = new Tile(-1, -1, TileType.NotVisible);
		pcs.firePropertyChange("tile", oldSelection, selTile);
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
		pcs.firePropertyChange("dimensions", dimensions, dim);
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
