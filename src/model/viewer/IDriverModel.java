package model.viewer;

import java.io.Serializable;

import model.map.MapView;
import util.PropertyChangeSource;

/**
 * An interface for driver-model objects that hold a mutable map. Interfaces
 * deriving from this one will give the methods each driver needs.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IDriverModel extends PropertyChangeSource, Serializable {

	/**
	 * @param newMap the new map
	 */
	void setMap(final MapView newMap);

	/**
	 *
	 * @return the map
	 */
	MapView getMap();

}
