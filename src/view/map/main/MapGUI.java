package view.map.main;

import model.viewer.MapModel;
import model.viewer.SPMap;
import model.viewer.Tile;
/**
 * An interface for a UI representing a map.
 * @author Jonathan Lovelace
 *
 */
public interface MapGUI {

	/**
	 * @return our visible dimensions
	 */
	VisibleDimensions getVisibleDimensions();

	/**
	 * Load and draw a subset of a map.
	 * 
	 * @param newMap
	 *            the map to load.
	 * @param minRow
	 *            the first row to draw
	 * @param maxRow
	 *            the last row to draw
	 * @param minCol
	 *            the first column to draw
	 * @param maxCol
	 *            the last column to draw
	 */
	void loadMap(final SPMap newMap, final int minRow, final int maxRow,
			final int minCol, final int maxCol);
	
	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	void loadMap(final SPMap newMap);

	/**
	 * @return the map we represent
	 */
	@Deprecated
	SPMap getMap();

	/**
	 * @param secMap
	 *            the new secondary map
	 */
	@Deprecated
	void setSecondaryMap(final SPMap secMap);

	/**
	 * Swap the main and secondary maps, i.e. show the secondary map
	 */
	void swapMaps();

	/**
	 * @return the secondary map
	 */
	@Deprecated
	SPMap getSecondaryMap();

	/**
	 * Copy a tile from the main map to the secondary map.
	 * 
	 * @param selection a tile in the relevant position.
	 */
	@Deprecated
	void copyTile(final Tile selection);
	/**
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	MapModel getModel();
}
