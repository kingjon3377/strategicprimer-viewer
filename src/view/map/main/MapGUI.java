package view.map.main;

import model.map.SPMap;
import model.viewer.MapModel;
import util.PropertyChangeSource;

/**
 * An interface for a UI representing a map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface MapGUI extends PropertyChangeSource {
	/**
	 * Load and draw a map.
	 * 
	 * @param newMap
	 *            the map to load
	 */
	void loadMap(final SPMap newMap);

	/**
	 * 
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	MapModel getModel();
	/**
	 * @return the size of a tile in this GUI.
	 */
	int getTileSize();
}
