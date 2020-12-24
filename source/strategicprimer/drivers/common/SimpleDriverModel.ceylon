import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.drivers.common {
    VersionChangeListener
}
import strategicprimer.model.common.map {
    SPMapNG,
    IMapNG,
    IMutableMapNG,
    MapDimensions,
    MapDimensionsImpl,
    PlayerCollection
}
import lovelace.util.common {
    invoke,
    PathWrapper
}

import ceylon.logging {
    Logger,
    logger
}

"Logger."
Logger log = logger(`module strategicprimer.drivers.common`);

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

    shared new (IMutableMapNG map = SPMapNG(MapDimensionsImpl(-1, -1, -1),
            PlayerCollection(), -1)) {
        mainMap = map;
        mapDim = mainMap.dimensions;
    }

    shared actual Boolean mapModified => map.modified;
    assign mapModified {
        restrictedMap.modified = mapModified;
        mcListeners.map(MapChangeListener.mapMetadataChanged).each(invoke); // FIXME: They should listen to the map, which should be a MapChangeSource
    }

    "Set a new main map."
    shared actual default void setMap(IMutableMapNG newMap) {
        for (listener in vcListeners) {
            listener.changeVersion(mapDim.version, newMap.dimensions.version);
        }
        mainMap = newMap;
        mapDim = newMap.dimensions;
        mcListeners.map(MapChangeListener.mapChanged).each(invoke);
    }

    "The (main) map."
    shared actual IMapNG map => mainMap;

    "The (main) map, for use by subclasses only."
    shared actual IMutableMapNG restrictedMap => mainMap;

    "The dimensions of the map."
    shared actual MapDimensions mapDimensions => mapDim;

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

    shared actual default Integer currentTurn => mainMap.currentTurn;
    assign currentTurn {
        mainMap.currentTurn = currentTurn;
        mainMap.modified = true;
    }

    shared actual void setMapFilename(PathWrapper filename) {
        if (exists existing = map.filename) {
            log.warn("Overwriting existing filename");
        }
        restrictedMap.filename = filename;
    }
}
