import ceylon.collection {
    MutableList,
    ArrayList
}

import java.nio.file {
    JPath=Path
}
import model.listeners {
    MapChangeListener,
    VersionChangeListener
}
import model.map {
    MapDimensions,
    MapDimensionsImpl,
    IMutableMapNG,
    SPMapNG,
    PlayerCollection
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
    "The filename from which the map was loaded, if known."
    variable JPath? mainMapFile;
    shared new (IMutableMapNG map = SPMapNG(MapDimensionsImpl(-1, -1, -1), PlayerCollection(), -1),
            JPath? file = null) {
        mainMap = map;
        mapDim = mainMap.dimensions();
        mainMapFile = file;
    }
    "Set a new main map."
    shared actual default void setMap(IMutableMapNG newMap, JPath? origin) {
        for (listener in vcListeners) {
            listener.changeVersion(mapDim.version, newMap.dimensions().version);
        }
        mainMap = newMap;
        mapDim = newMap.dimensions();
        mainMapFile = origin;
        for (listener in mcListeners) {
            listener.mapChanged();
        }
    }
    "The (main) map."
    shared actual IMutableMapNG map => mainMap;
    "The dimensions of the map."
    shared actual MapDimensions mapDimensions => mapDim;
    "The filename from which the map was loaded, if known."
    shared actual JPath? mapFile => mainMapFile;
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