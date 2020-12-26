import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.common.map {
    IMapNG,
    IMutableMapNG
}

"A superclass for implementations of interfaces inheriting from [[IMultiMapModel]]."
shared class SimpleMultiMapModel extends SimpleDriverModel satisfies IMultiMapModel {
    "The collection of subordinate maps."
    MutableList<IMutableMapNG> subordinateMapsList = ArrayList<IMutableMapNG>();

    "Subordinate maps and the files from which they were loaded."
    shared actual {IMapNG*} subordinateMaps => subordinateMapsList.sequence();

    "Subordinate maps and the files from which they were loaded, for use by subclasses only."
    shared actual {<IMutableMapNG>*} restrictedSubordinateMaps => subordinateMapsList.sequence();

    shared new (IMutableMapNG map) extends SimpleDriverModel(map) { }

    shared new copyConstructor(IDriverModel model) extends SimpleDriverModel(model.restrictedMap) {
        if (is IMultiMapModel model) {
            subordinateMapsList.addAll(model.restrictedSubordinateMaps);
        }
    }

    shared actual void addSubordinateMap(IMutableMapNG map) => subordinateMapsList.add(map);

    shared actual IDriverModel? fromSecondMap() {
        if (exists map = restrictedSubordinateMaps.first) {
            return SimpleDriverModel(map);
        } else {
            return null;
        }
    }

    shared actual Integer currentTurn =>
            allMaps.map(IMapNG.currentTurn).find(not(Integer.negative)) else map.currentTurn;

    assign currentTurn {
        for (map in restrictedAllMaps) {
            map.currentTurn = currentTurn;
            map.modified = true;
        }
    }

    shared actual void setMapModified(IMapNG map, Boolean flag) {
        for (subMap in restrictedAllMaps) {
            if (subMap === map) {
                subMap.modified = flag;
                return;
            }
        }
        for (subMap in restrictedAllMaps) {
            if (subMap == map) {
                subMap.modified = flag;
                return;
            }
        }
    }

    shared actual void clearModifiedFlag(IMapNG map) {
        for (subMap in restrictedAllMaps) {
            if (subMap === map) {
                subMap.modified = false;
                return;
            }
        }
        for (subMap in restrictedAllMaps) {
            if (subMap == map) {
                subMap.modified = false;
                return;
            }
        }
    }
}
