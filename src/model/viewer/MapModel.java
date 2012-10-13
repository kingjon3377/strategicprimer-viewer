package model.viewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileType;
import util.PropertyChangeSource;

/**
 * A class to encapsulate the various model-type things views need to do with
 * maps.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MapModel implements PropertyChangeSource {
	/**
	 * Constructor.
	 *
	 * @param firstMap the initial map
	 */
	public MapModel(final MapView firstMap) {
		setMainMap(firstMap);
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
	 * The secondary map.
	 */
	private MapView secondaryMap;
	/**
	 * The currently selected tile in the main map.
	 */
	private Tile selTile;
	/**
	 * The currently-selected tile in the secondary map.
	 */
	private Tile secondTile;

	/**
	 * @param newMap the new map
	 */
	public void setMainMap(final MapView newMap) {
		pcs.firePropertyChange("version", map == null ? 0 : map.getVersion(),
				newMap.getVersion());
		map = newMap;
		setSecondaryMap(new MapView(new SPMap(map.getVersion(), map.rows(),
				map.cols()), map.getPlayers().getCurrentPlayer()
				.getPlayerId(), map.getCurrentTurn()));
		// TODO: Perhaps clearSelection() instead of setting to (-1, -1)?
		setSelection(PointFactory.point(-1, -1));
		setDimensions(new VisibleDimensions(0, getSizeRows() - 1, 0,
				getSizeCols() - 1));
		pcs.firePropertyChange("map", map, newMap);
	}

	/**
	 * @param newMap the new secondary map
	 */
	public void setSecondaryMap(final MapView newMap) {
		if (newMap.rows() == map.rows() && newMap.cols() == map.cols()) {
			pcs.firePropertyChange("secondary-map", secondaryMap, newMap);
			secondaryMap = newMap;
		} else {
			throw new IllegalArgumentException("Map sizes must match");
		}
	}

	/**
	 *
	 * @return the currently selected tile
	 */
	public Tile getSelectedTile() {
		return selTile;
	}

	/**
	 *
	 * @return the currently selected tile in the secondary map
	 */
	public Tile getSecondarySelectedTile() {
		return secondTile;
	}

	/**
	 * Set the new selected tiles, given coordinates.
	 *
	 * @param point the location of the new tile.
	 */
	public void setSelection(final Point point) {
		final Tile oldSelection = selTile;
		final Tile oldSecSelection = secondTile;
		selTile = map.getTile(point);
		secondTile = secondaryMap.getTile(point);
		pcs.firePropertyChange("tile", oldSelection, selTile);
		pcs.firePropertyChange("secondary-tile", oldSecSelection, secondTile);
	}

	/**
	 * Swap the maps.
	 */
	public void swapMaps() {
		final MapView temp = map;
		setMainMap(secondaryMap);
		setSecondaryMap(temp);
	}

	/**
	 * Copy a tile from the main map to the secondary map.
	 *
	 * @param selection the tile to copy.
	 */
	public void copyTile(final Tile selection) {
		secondaryMap.getTile(selection.getLocation()).update(
				map.getTile(selection.getLocation()));
	}

	/**
	 *
	 * @return the size of the map in rows
	 */
	public int getSizeRows() {
		return map.rows();
	}

	/**
	 *
	 * @return the size of the map in columns
	 */
	public int getSizeCols() {
		return map.cols();
	}

	/**
	 * @param point a tile's location
	 *
	 * @return the tile at that location
	 */
	public Tile getTile(final Point point) {
		return map.getTile(point);
	}

	/**
	 *
	 * @return the main map
	 */
	public MapView getMainMap() {
		return map;
	}

	/**
	 *
	 * @return the secondary map
	 */
	public MapView getSecondaryMap() {
		return secondaryMap;
	}

	/**
	 * Clear the selection.
	 */
	public void clearSelection() {
		final Tile oldSelection = selTile;
		final Tile oldSecSelection = secondTile;
		selTile = new Tile(-1, -1, TileType.NotVisible);
		secondTile = new Tile(-1, -1, TileType.NotVisible);
		pcs.firePropertyChange("tile", oldSelection, selTile);
		pcs.firePropertyChange("secondary-tile", oldSecSelection, secondTile);
	}

	/**
	 * The visible dimensions of the map.
	 */
	private VisibleDimensions dimensions;

	/**
	 * @param dim the new visible dimensions of the map
	 */
	public void setDimensions(final VisibleDimensions dim) {
		pcs.firePropertyChange("dimensions", dimensions, dim);
		dimensions = dim;
	}

	/**
	 *
	 * @return the visible dimensions of the map
	 */
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
