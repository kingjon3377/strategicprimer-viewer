import strategicprimer.model.common.map {
    IMutableMapNG,
    MapDimensions
}

import lovelace.util.common {
    PathWrapper
}

"An interface for driver-model objects that hold a mutable map. Interfaces deriving from
 this one will give the methods each driver needs."
shared interface IDriverModel satisfies MapChangeSource&VersionChangeSource {
    "Set the (main) map and its filename."
    shared formal void setMap("The new map" IMutableMapNG newMap,
            "The file from which it was loaded, if known" PathWrapper? origin,
            "Whether it has been modified since it was loaded or last saved"
            Boolean modified = false);

    "The (main) map."
    shared formal IMutableMapNG map;

    "Its dimensions."
    shared default MapDimensions mapDimensions => map.dimensions;

    "The filename from which the map was loaded or to which it should be written."
    shared formal variable PathWrapper? mapFile;

    "Whether the map has been changed since it was loaded or last saved."
    shared formal variable Boolean mapModified;
}
