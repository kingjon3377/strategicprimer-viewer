import java.nio.file {
    JPath=Path
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IMutableMapNG,
    IMapNG
}
"""A driver-model for drivers that have a main map (like every driver) and any number of
   "subordinate" maps."""
shared interface IMultiMapModel satisfies IDriverModel {
    "Add a subordinate map."
    shared formal void addSubordinateMap(
            "The map to add"
            IMutableMapNG map,
            "The file it was loaded from"
            JPath? file);
    "Remove a subordinate map."
    todo("Allow callers to remove by filename")
    shared formal void removeSubordinateMap(
            "The map to remove"
            IMapNG map);
    "Subordinate maps with their filenames."
    shared formal {[IMutableMapNG, JPath?]*} subordinateMaps;
    "All maps with their filenames, including the main map and the subordinate maps."
    todo("Move implementation from SimpleMultiMapModel to a default one here")
    shared formal {[IMutableMapNG, JPath?]*} allMaps;
}