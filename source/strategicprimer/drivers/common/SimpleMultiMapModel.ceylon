import ceylon.collection {
    MutableList,
    ArrayList
}

import java.nio.file {
    JPath=Path,
    JPaths=Paths
}

import strategicprimer.model.map {
    IMutableMapNG
}
JPath emptyPath = JPaths.get("");
"A superclass for implementations of interfaces inheriting from [[IMultiMapModel]]."
shared class SimpleMultiMapModel extends SimpleDriverModel satisfies IMultiMapModel {
    "The collection of subordinate maps."
    MutableList<[IMutableMapNG, JPath?]> subordinateMapsList =
            ArrayList<[IMutableMapNG, JPath?]>();
    shared actual {[IMutableMapNG, JPath?]*} subordinateMaps =>
            {*subordinateMapsList};
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
