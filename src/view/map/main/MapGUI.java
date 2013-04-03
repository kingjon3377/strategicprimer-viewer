package view.map.main;

import model.misc.IDriverModel;
import util.PropertyChangeSource;

/**
 * An interface for a UI representing a map.
 *
 * @author Jonathan Lovelace
 *
 */
public interface MapGUI extends PropertyChangeSource {
	/**
	 *
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	IDriverModel getMapModel();
}
