import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    JavaIterable,
    CeylonIterable
}

import java.lang {
    JIterable=Iterable
}
import java.nio.file {
    JPath=Path
}
import java.util {
    JOptional=Optional
}

import lovelace.util.common {
    todo
}

import model.map {
    IMutableMapNG,
    IMapNG
}
import model.misc {
    SimpleDriverModel,
    IDriverModel
}

import util {
    Pair
}
"A superclass for implementations of interfaces inheriting from [[IMultiMapModel]]."
shared class SimpleMultiMapModel extends SimpleDriverModel satisfies IMultiMapModel {
    "The collection of subordinate maps."
    MutableList<[IMutableMapNG, JPath?]> subordinateMapsList =
            ArrayList<[IMutableMapNG, JPath?]>();
    shared new (IMutableMapNG map, JOptional<JPath> file) extends SimpleDriverModel() {
        super.setMap(map, file);
    }
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel(model.map, model.mapFile) {
        if (is IMultiMapModel model) {
            for (pair in model.subordinateMaps) {
                subordinateMapsList.add(pair);
            }
        }
    }
    shared actual void addSubordinateMap(IMutableMapNG map, JPath? file) =>
            subordinateMapsList.add([map, file]);
    todo(/*FIXME*/"Test this; I fixed the clearly-wrong implementation, but this might
                   cause [[ConcurrentModificationException]]")
    shared actual void removeSubordinateMap(IMapNG map) {
        subordinateMapsList.removeWhere(([localMap, file]) => localMap == map);
    }
    shared actual {[IMutableMapNG, JPath?]*} subordinateMaps =>
            {*subordinateMapsList};
    shared actual {[IMutableMapNG, JPath?]*} allMaps =>
            subordinateMaps.follow([map, mapFile.orElse(null)]);
}
