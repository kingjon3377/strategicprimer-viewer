import java.nio.file {
    JPath=Path
}

import strategicprimer.model.map {
    IMutableMapNG
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
    "Subordinate maps with their filenames."
    shared formal {[IMutableMapNG, JPath?]*} subordinateMaps;
    "All maps with their filenames, including the main map and the subordinate maps."
    shared default {[IMutableMapNG, JPath?]*} allMaps =>
            subordinateMaps.follow([map, mapFile]);
}