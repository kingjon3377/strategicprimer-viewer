import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IMutableMap,
    IMap
}
"""A driver-model for drivers that have a main map (like every driver) and any number of
   "subordinate" maps."""
shared interface IMultiMapModel satisfies IDriverModel {
    "Add a subordinate map."
    shared formal void addSubordinateMap(
            "The map to add"
            IMutableMap map,
            "The file it was loaded from"
            JPath? file);
    "Remove a subordinate map."
    todo("Allow callers to remove by filename")
    shared formal void removeSubordinateMap(
            "The map to remove"
            IMap map);
    "Subordinate maps with their filenames."
    shared formal {[IMutableMap, JPath?]*} subordinateMaps;
    "All maps with their filenames, including the main map and the subordinate maps."
    shared default {[IMutableMap, JPath?]*} allMaps =>
            subordinateMaps.follow([map, mapFile]);
}