package model.misc;

import java.util.ArrayList;
import java.util.List;

import model.listeners.MapChangeListener;
import model.listeners.VersionChangeListener;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.SPMap;

/**
 * A superclass for driver-models, to handle the common details.
 *
 * @author Jonathan Lovelace
 *
 */
// ESCA-JAVA0011:
public abstract class AbstractDriverModel implements IDriverModel {
	/**
	 * The list of map-change listeners.
	 */
	private final List<MapChangeListener> mcListeners = new ArrayList<>();
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
		for (final VersionChangeListener list : vcListeners) {
			list.changeVersion(mapDim.version, newMap.getDimensions().version);
		}
		map = newMap;
		mapDim = newMap.getDimensions();
		filename = name;
		for (final MapChangeListener list : mcListeners) {
			list.mapChanged();
		}
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
	 * @return the filename from which the map was loaded or to which it should
	 *         be saved
	 */
	@Override
	public String getMapFilename() {
		return filename;
	}

	/**
	 * Add a MapChangeListener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public void addMapChangeListener(final MapChangeListener list) {
		mcListeners.add(list);
	}

	/**
	 * Remove a MapChangeListener.
	 *
	 * @param list the listener to remove
	 */
	@Override
	public void removeMapChangeListener(final MapChangeListener list) {
		mcListeners.remove(list);
	}

	/**
	 * The list of version change listeners.
	 */
	private final List<VersionChangeListener> vcListeners = new ArrayList<>();

	/**
	 * Add a version change listener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public void addVersionChangeListener(final VersionChangeListener list) {
		vcListeners.add(list);
	}

	/**
	 * Removve a version-change listener.
	 *
	 * @param list the listener to remove
	 */
	@Override
	public void removeVersionChangeListener(final VersionChangeListener list) {
		vcListeners.remove(list);
	}
}
