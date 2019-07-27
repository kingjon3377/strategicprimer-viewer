import lovelace.util.common {
    DRMap=DelayedRemovalMap,
    comparingOn,
    narrowedStream
}

import strategicprimer.model.common.map {
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    maturityModel,
    animalPlurals,
    AnimalTracks,
    AnimalOrTracks
}

import com.vasileff.ceylon.structures {
    ArrayListMultimap,
    MutableMultimap
}

"A report generator for sightings of animals."
shared class AnimalReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        MapDimensions dimensions, Integer currentTurn, Point? hq = null)
        extends AbstractReportGenerator</*Animal|AnimalTracks*/AnimalOrTracks>(comp,
            dimensions, hq) {
    "Produce the sub-report about an individual Animal. We assume that individual
     Animals are members of the player's units, or that for some other reason the player
     is allowed to see the precise count of the population."
    shared actual void produceSingle(DRMap<Integer, [Point, IFixture]> fixtures,
                IMapNG map, Anything(String) ostream,
                /*Animal|AnimalTracks*/AnimalOrTracks item, Point loc) {
            ostream("At ``loc``:");
            if (is AnimalTracks item) {
                ostream(" tracks or traces of ``item.kind``");
            } else {
                if (item.talking) {
                    ostream(" talking");
                }
                if (item.born >= 0, currentTurn >= 0) {
                    if (item.born > currentTurn) {
                        ostream(" unborn");
                    } else if (item.born == currentTurn) {
                        ostream(" newborn");
                    } else if (exists maturityAge = maturityModel.maturityAges[item.kind],
                        maturityAge <= (currentTurn - item.born)) {
                        // do nothing
                    } else {
                        ostream(" ``currentTurn - item.born``-turn-old");
                    }
                }
                if (item.population == 1) {
                    ostream(" ``item.kind``");
                } else {
//                    ostream(" ``item.population`` ``animalPlurals[item.kind]``"); // TODO: syntax sugar once compiler bug fixed
                    ostream(" ``item.population`` ``animalPlurals.get(item.kind)``");
                    if (item.status == "wild") {
                        ostream(" (ID # ``item.id``)");
                    }
                }
            }
            ostream(" ``distanceString(loc)``");
    }

    "Produce the sub-report about animals."
    shared actual void produce(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
                Anything(String) ostream) {
        MutableMultimap<String, Point> items = ArrayListMultimap<String, Point>();
        for (key->[loc, animal] in
                narrowedStream<Integer, [Point, Animal|AnimalTracks]>(fixtures)
                .sort(comparingOn(Entry<Integer, [Point, IFixture]>.item,
                    pairComparator))) {
            String desc;
            if (is AnimalTracks animal) {
                desc = "tracks or traces of ``animal.kind``";
            } else if (animal.talking) {
                desc = "talking ``animal.kind``";
            } else {
                desc = animal.kind;
            }
            items.put(desc, loc);
            fixtures.remove(key);
        }
        if (!items.empty) {
            ostream("""<h4>Animal sightings or encounters</h4>
                        <ul>
                        """);
            for (key->list in items.asMap) {
                if (!list.empty) {
                    ostream("<li>``key``: at ``commaSeparatedList(*list)``</li>
                         ");
                }
            }
            ostream("""</ul>
                   """);
        }
    }
}
