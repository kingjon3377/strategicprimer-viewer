package model.viewer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import view.util.PropertyChangeSource;
/**
 * A class to encapsulate the various model-type things views need to do with maps.
 * @author Jonathan Lovelace
 *
 */
public final class MapModel implements PropertyChangeSource {
	/**
	 * Constructor.
	 * @param firstMap the initial map
	 */
	public MapModel(final SPMap firstMap) {
		setMainMap(firstMap);
	}
	/**
	 * A helper object to handle property-change listeners for us.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 * Add a property-change listener.
	 * @param list the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}
	/**
	 * Remove a property-change listener.
	 * @param list the listener to remove.
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
	/**
	 * The main map.
	 */
	private SPMap map;
	/**
	 * The secondary map.
	 */
	private SPMap secondaryMap;
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
	public void setMainMap(final SPMap newMap) {
		map = newMap;
		setSecondaryMap(new SPMap(map.rows(), map.cols()));
		setSelection(-1, -1);
	}
	/**
	 * @param newMap the new secondary map
	 */
	public void setSecondaryMap(final SPMap newMap) {
		if (newMap.rows() == map.rows() && newMap.cols() == map.cols()) {
			secondaryMap = newMap;
		} else {
			throw new IllegalArgumentException("Map sizes must match");
		}
	}
	/**
	 * @return the currently selected tile
	 */
	public Tile getSelectedTile() {
		return selTile;
	}
	/**
	 * @return the currently selected tile in the secondary map
	 */
	public Tile getSecondarySelectedTile() {
		return secondTile;
	}
	/**
	 * Set the new selected tiles, given coordinates.
	 * @param row the row coordinate
	 * @param col the column coordinate.
	 */
	public void setSelection(final int row, final int col) {
		final Tile oldSelection = selTile;
		final Tile oldSecSelection = secondTile;
		selTile = map.getTile(row, col);
		secondTile = secondaryMap.getTile(row, col);
		pcs.firePropertyChange("tile", oldSelection, selTile);
		pcs.firePropertyChange("secondary-tile", oldSecSelection, secondTile);
	}
	/**
	 * Swap the maps.
	 */
	public void swapMaps() {
		final SPMap temp = map;
		setMainMap(secondaryMap);
		setSecondaryMap(temp);
	}
	/**
	 * Copy a tile from the main map to the secondary map.
	 * @param selection the tile to copy.
	 */
	public void copyTile(final Tile selection) {
		secondaryMap.getTile(selection.getRow(), selection.getCol()).update(
				map.getTile(selection.getRow(), selection.getCol()));
	}
	/**
	 * @return the size of the map in rows
	 */
	public int getSizeRows() {
		return map.rows();
	}
	/**
	 * @return the size of the map in columns
	 */
	public int getSizeCols() {
		return map.cols();
	}
	/**
	 * @param row the row of a tile
	 * @param col the column of a tile
	 * @return the tile at those coordinates
	 */
	public Tile getTile(final int row, final int col) {
		return map.getTile(row, col);
	}
	/**
	 * @param row the row of a tile
	 * @param col the column of a tile
	 * @return the tile in the secondary map at those coordinates
	 * @deprecated MapModel should be used for handling the selection instead.
	 */
	@Deprecated
	public Tile getSecondaryTile(final int row, final int col) {
		return secondaryMap.getTile(row, col);
	}
	/**
	 * @return the main map
	 */
	public SPMap getMainMap() {
		return map;
	}
	/**
	 * @return the secondary map
	 */
	public SPMap getSecondaryMap() {
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
}
