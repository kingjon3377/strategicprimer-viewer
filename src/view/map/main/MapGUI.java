package view.map.main;

import model.viewer.MapModel;
import model.viewer.SPMap;
/**
 * An interface for a UI representing a map.
 * @author Jonathan Lovelace
 *
 */
public interface MapGUI {
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
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	MapModel getModel();
}
