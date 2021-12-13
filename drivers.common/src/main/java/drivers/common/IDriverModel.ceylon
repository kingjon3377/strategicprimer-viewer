import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG,
    MapDimensions
}

import lovelace.util.common {
    PathWrapper
}

"An interface for driver-model objects that hold a mutable map. Interfaces deriving from
 this one will give the methods each driver needs."
shared interface IDriverModel satisfies MapChangeSource&VersionChangeSource {
    "Set the (main) map"
    shared formal void setMap("The new map" IMutableMapNG newMap);
    "The (main) map."
    shared formal IMapNG map;

    "Set the map filename."
    shared formal void setMapFilename(PathWrapper filename);

    "The (main) map, for use by subclasses only."
    shared formal IMutableMapNG restrictedMap;

    "Its dimensions."
    shared default MapDimensions mapDimensions => map.dimensions;

    "Whether the map has been changed since it was loaded or last saved."
    shared default Boolean mapModified => map.modified;
    assign mapModified {
        restrictedMap.modified = mapModified;
    }

    "The current turn for the map."
    shared formal variable Integer currentTurn;
}
