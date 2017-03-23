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
    IMultiMapModel,
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
                subordinateMapsList.add([pair.first(), pair.second().orElse(null)]);
            }
        }
    }
    shared actual void addSubordinateMap(IMutableMapNG map, JOptional<JPath> file) =>
            subordinateMapsList.add([map, file.orElse(null)]);
    todo(/*FIXME*/"Test this; I fixed the clearly-wrong implementation, but this might
                   cause [[ConcurrentModificationException]]")
    shared actual void removeSubordinateMap(IMapNG map) {
        subordinateMapsList.removeWhere(([localMap, file]) => localMap == map);
    }
    shared actual JIterable<Pair<IMutableMapNG, JOptional<JPath>>> subordinateMaps =>
            JavaIterable(subordinateMapsList
                .map(([localMap, path]) => Pair.\iof<IMutableMapNG, JOptional<JPath>>(
                    localMap, JOptional.ofNullable(path))));
    shared actual JIterable<Pair<IMutableMapNG, JOptional<JPath>>> allMaps =>
            JavaIterable(CeylonIterable(subordinateMaps)
                .follow(Pair.\iof<IMutableMapNG, JOptional<JPath>>(map, mapFile)));
}
