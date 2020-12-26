import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG
}

"""A driver-model for drivers that have a main map (like every driver) and any number of
   "subordinate" maps."""
shared interface IMultiMapModel satisfies IDriverModel {
    "Add a subordinate map."
    shared formal void addSubordinateMap(
            "The map to add"
            IMutableMapNG map);

    "Subordinate maps"
    shared formal {IMapNG*} subordinateMaps;

    "Subordinate maps. For use by subclasses only."
    shared formal {IMutableMapNG*} restrictedSubordinateMaps;

    "All maps."
    shared default {IMapNG*} allMaps => subordinateMaps.follow(map);

    "All maps. For use by subclasses only."
    shared default {IMutableMapNG*} restrictedAllMaps =>
            restrictedSubordinateMaps.follow(restrictedMap);

    "A driver model with the second map as its first or sole map."
    shared formal IDriverModel? fromSecondMap();

    "Set the modified flag on the given map."
    deprecated("Modification to the map should ideally only come through model methods")
    shared formal void setMapModified(IMapNG map, Boolean flag);

    "Clear the modified flag on the given map. (For the code that saves the map to file.)"
    shared formal void clearModifiedFlag(IMapNG map);
}
