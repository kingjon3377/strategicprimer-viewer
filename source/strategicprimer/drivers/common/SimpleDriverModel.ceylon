import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.drivers.common {
    VersionChangeListener
}
import strategicprimer.model.common.map {
    SPMapNG,
    IMutableMapNG,
    MapDimensions,
    MapDimensionsImpl,
    PlayerCollection
}
import lovelace.util.common {
    PathWrapper
}

"A superclass for driver-models, to handle the common details."
shared class SimpleDriverModel satisfies IDriverModel {
    "The list of map-change listeners."
    MutableList<MapChangeListener> mcListeners = ArrayList<MapChangeListener>();
    "The list of version change listeners."
    MutableList<VersionChangeListener> vcListeners = ArrayList<VersionChangeListener>();
    "The dimensions of the map."
    variable MapDimensions mapDim;
    "The main map."
    variable IMutableMapNG mainMap;
    "Whether the map has been changed since it was loaded or last saved."
    variable Boolean modifiedFlag;
    "The filename from which the map was loaded, if known."
    variable PathWrapper? mainMapFile;
    shared new (IMutableMapNG map = SPMapNG(MapDimensionsImpl(-1, -1, -1),
            PlayerCollection(), -1), PathWrapper? file = null, Boolean modified = false) {
        mainMap = map;
        mapDim = mainMap.dimensions;
        mainMapFile = file;
        modifiedFlag = modified;
    }
    shared actual Boolean mapModified => modifiedFlag;
    assign mapModified {
        modifiedFlag = mapModified;
        for (listener in mcListeners) {
            // Iterable.each(MapChangeListener.mapMetadataChanged) does *not* work!
            listener.mapMetadataChanged();
        }
    }
    "Set a new main map."
    shared actual default void setMap(IMutableMapNG newMap, PathWrapper? origin,
            Boolean modified) {
        for (listener in vcListeners) {
            listener.changeVersion(mapDim.version, newMap.dimensions.version);
        }
        mainMap = newMap;
        mapDim = newMap.dimensions;
        mainMapFile = origin;
        mapModified = modified;
        for (listener in mcListeners) {
            listener.mapChanged();
        }
    }
    "The (main) map."
    shared actual IMutableMapNG map => mainMap;
    "The dimensions of the map."
    shared actual MapDimensions mapDimensions => mapDim;
    "The filename from which the map was loaded, if known."
    shared actual PathWrapper? mapFile => mainMapFile;
    assign mapFile {
        mainMapFile = mapFile;
        for (listener in mcListeners) {
            // Iterable.each(MapChangeListener.mapMetadataChanged) does *not* work!
            listener.mapMetadataChanged();
        }
    }
    "Add a map-change listener."
    shared actual void addMapChangeListener(MapChangeListener listener) =>
            mcListeners.add(listener);
    "Remove a map-change listener."
    shared actual void removeMapChangeListener(MapChangeListener listener) =>
            mcListeners.remove(listener);
    "Add a version-change listener."
    shared actual void addVersionChangeListener(VersionChangeListener listener) =>
            vcListeners.add(listener);
    "Remove a version-change listener."
    shared actual void removeVersionChangeListener(VersionChangeListener listener) =>
            vcListeners.remove(listener);
}
