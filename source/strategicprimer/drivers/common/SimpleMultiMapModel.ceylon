import ceylon.collection {
    MutableList,
    ArrayList
}

import java.nio.file {
    JPath=Path
}

import strategicprimer.model.map {
    IMutableMapNG
}
"A superclass for implementations of interfaces inheriting from [[IMultiMapModel]]."
shared class SimpleMultiMapModel extends SimpleDriverModel satisfies IMultiMapModel {
    "The collection of subordinate maps."
    MutableList<[IMutableMapNG, JPath?]> subordinateMapsList =
            ArrayList<[IMutableMapNG, JPath?]>();
    "Subordinate maps and the files from which they were loaded."
    shared actual {[IMutableMapNG, JPath?]*} subordinateMaps =>
            subordinateMapsList.sequence();
    shared new (IMutableMapNG map, JPath? file)
            extends SimpleDriverModel(map, file) { }
    shared new copyConstructor(IDriverModel model)
            extends SimpleDriverModel(model.map, model.mapFile) {
        if (is IMultiMapModel model) {
            subordinateMapsList.addAll(model.subordinateMaps);
        }
    }
    shared actual void addSubordinateMap(IMutableMapNG map, JPath? file) =>
            subordinateMapsList.add([map, file]);
}
