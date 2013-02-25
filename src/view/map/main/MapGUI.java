package view.map.main;

import model.map.MapView;
import model.misc.IDriverModel;
import model.viewer.TileViewSize;
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
	 * @param newMap the map to load
	 * @param filename the filename it's loaded from
	 */
	void loadMap(final MapView newMap, String filename);

	/**
	 *
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	IDriverModel getMapModel();

	/**
	 * @return an object that will tell us the size of a tile in this GUI.
	 */
	TileViewSize getTileSize();
}
