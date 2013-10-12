package model.misc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import model.map.MapDimensions;
import model.map.MapView;
import model.map.SPMap;

import org.eclipse.jdt.annotation.Nullable;
/**
 * A superclass for driver-models, to handle the common details.
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0011:
public abstract class AbstractDriverModel implements IDriverModel {
	/**
	 * A helper object to handle property-change listeners for us.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	/**
	 *
	 */
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
	 * The dimensions of the map.
	 */
	private MapDimensions mapDim = new MapDimensions(-1, -1, -1);
	/**
	 * The main map.
	 */
	private MapView map = new MapView(new SPMap(mapDim), -1, -1);
	/**
	 * The name from which the map was loaded.
	 */
	private String filename = "";
	/**
	 * @param newMap the new map
	 * @param name the filename from which the map was loaded
	 */
	@Override
	public void setMap(final MapView newMap, final String name) {
		if (mapDim == null) {
			pcs.firePropertyChange("version", -1, newMap.getDimensions()
					.getVersion());
		} else {
			pcs.firePropertyChange("version", mapDim.version,
					newMap.getDimensions().version);
		}
		map = newMap;
		mapDim = newMap.getDimensions();
		filename = name;
		pcs.firePropertyChange("map", map, newMap);
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
	 * @return the dimensions and version of the map
	 */
	@Override
	public MapDimensions getMapDimensions() {
		return mapDim;
	}
	/**
	 * Report a property change to listeners.
	 * @param propertyName the  name of the property
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	protected void firePropertyChange(final String propertyName, @Nullable final Object oldValue, final Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}
	/**
	 * @return the filename from which the map was loaded or to which it should be saved
	 */
	@Override
	public String getMapFilename() {
		return filename;
	}
}
