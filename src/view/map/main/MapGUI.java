package view.map.main;

import model.misc.IDriverModel;

/**
 * An interface for a UI representing a map.
 *
 * @author Jonathan Lovelace
 *
 */
public interface MapGUI {
	/**
	 *
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	IDriverModel getMapModel();
}
