import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.common.map {
    IMutableMapNG
}
import lovelace.util.common {
    matchingValue,
    PathWrapper
}

"A superclass for implementations of interfaces inheriting from [[IMultiMapModel]]."
shared class SimpleMultiMapModel extends SimpleDriverModel satisfies IMultiMapModel {
    "The collection of subordinate maps."
    MutableList<IMutableMapNG->[PathWrapper?, Boolean]> subordinateMapsList =
            ArrayList<IMutableMapNG->[PathWrapper?, Boolean]>();
    "Subordinate maps and the files from which they were loaded."
    shared actual {<IMutableMapNG->[PathWrapper?, Boolean]>*} subordinateMaps =>
            subordinateMapsList.sequence();
    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
            extends SimpleDriverModel(map, file, modified) { }
    shared new copyConstructor(IDriverModel model)
            extends SimpleDriverModel(model.map, model.mapFile, model.mapModified) {
        if (is IMultiMapModel model) {
            subordinateMapsList.addAll(model.subordinateMaps);
        }
    }
    shared actual void addSubordinateMap(IMutableMapNG map, PathWrapper? file,
            Boolean modified) => subordinateMapsList.add(map->[file, modified]);
    shared actual void setModifiedFlag(IMutableMapNG map, Boolean modified) {
        if (map == this.map) {
            mapModified = modified;
        } else if (exists index->entry = subordinateMapsList.locate(matchingValue(map,
                Entry<IMutableMapNG, [PathWrapper?, Boolean]>.key))) {
            if (entry.item.rest.first != modified) {
                subordinateMapsList[index] = entry.key->[entry.item.first, modified];
            }
        }
    }
}
