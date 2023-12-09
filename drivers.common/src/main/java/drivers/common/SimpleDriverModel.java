package drivers.common;

import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;

import legacy.map.LegacyMap;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.MapDimensions;
import legacy.map.MapDimensionsImpl;
import common.map.PlayerCollection;

import lovelace.util.LovelaceLogger;

/**
 * A superclass for driver-models, to handle the common details.
 */
public class SimpleDriverModel implements IDriverModel {
	/**
	 * The list of map-change listeners.
	 */
	private final List<MapChangeListener> mcListeners = new ArrayList<>();

	/**
	 * The list of version change listeners.
	 */
	private final List<VersionChangeListener> vcListeners = new ArrayList<>();

	/**
	 * The dimensions of the map.
	 */
	private MapDimensions mapDim;

	/**
	 * The main map.
	 */
	private IMutableLegacyMap mainMap;

	public SimpleDriverModel() {
		this(new LegacyMap(new MapDimensionsImpl(-1, -1, -1), new PlayerCollection(), -1));
	}

	public SimpleDriverModel(final IMutableLegacyMap map) {
		mainMap = map;
		mapDim = mainMap.getDimensions();
	}

	@Override
	public final boolean isMapModified() {
		return mainMap.isModified();
	}

	@Override
	public final void setMapModified(final boolean mapModified) {
		mainMap.setModified(mapModified);
		for (final MapChangeListener listener : mcListeners) {
			listener.mapMetadataChanged();
		}
	}

	/**
	 * Set a new main map.
	 */
	@Override
	public void setMap(final IMutableLegacyMap newMap) {
		for (final VersionChangeListener listener : vcListeners) {
			listener.changeVersion(mapDim.version(), newMap.getDimensions().version());
		}
		mainMap = newMap;
		mapDim = newMap.getDimensions();
		for (final MapChangeListener listener : mcListeners) {
			listener.mapChanged();
		}
	}

	/**
	 * The (main) map.
	 */
	@Override
	public final ILegacyMap getMap() {
		return mainMap;
	}

	/**
	 * The (main) map, for use by subclasses only. TODO: Make a read-only
	 * wrapper implementation of IMapNG?
	 */
	@Override
	public final IMutableLegacyMap getRestrictedMap() {
		return mainMap;
	}

	/**
	 * The dimensions of the map.
	 */
	@Override
	public final MapDimensions getMapDimensions() {
		return mapDim;
	}

	/**
	 * Add a map-change listener.
	 */
	@Override
	public final void addMapChangeListener(final MapChangeListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * Remove a map-change listener.
	 */
	@Override
	public final void removeMapChangeListener(final MapChangeListener listener) {
		mcListeners.remove(listener);
	}

	/**
	 * Add a version-change listener.
	 */
	@Override
	public final void addVersionChangeListener(final VersionChangeListener listener) {
		vcListeners.add(listener);
	}

	/**
	 * Remove a version-change listener.
	 */
	@Override
	public final void removeVersionChangeListener(final VersionChangeListener listener) {
		vcListeners.remove(listener);
	}

	@Override
	public int getCurrentTurn() {
		return mainMap.getCurrentTurn();
	}

	@Override
	public void setCurrentTurn(final int currentTurn) {
		mainMap.setCurrentTurn(currentTurn);
		mainMap.setModified(true);
	}

	@Override
	public final void setMapFilename(final Path filename) {
		if (mainMap.getFilename() != null) {
			LovelaceLogger.warning("Overwriting existing filename");
		}
		mainMap.setFilename(filename);
	}
}
