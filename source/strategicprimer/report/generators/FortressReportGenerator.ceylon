import ceylon.collection {
    MutableList,
    MutableMap,
    ArrayList,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    River,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    Implement,
    ResourcePile,
    FortressMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.model.common.map.fixtures.terrain {
    Oasis,
    Hill,
    Forest
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}

import com.vasileff.ceylon.structures {
    MutableMultimap,
    ArrayListMultimap
}

"A report generator for fortresses."
shared class FortressReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Integer currentTurn, Point? hq = null)
        extends AbstractReportGenerator<Fortress>(comp, dimensions, hq) {
    IReportGenerator<IUnit> urg =
            UnitReportGenerator(comp, currentPlayer, dimensions, currentTurn, hq);
    IReportGenerator<FortressMember> memberReportGenerator =
            FortressMemberReportGenerator(comp, currentPlayer, dimensions, currentTurn,
                hq);

    String terrain(IMapNG map, Point point,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures) {
        StringBuilder builder = StringBuilder();
        builder.append("Surrounding terrain: ``map.baseTerrain[point] else "Unknown"``");
        variable Boolean unforested = true;
//        if (map.mountainous[point]) { // TODO: syntax sugar once compiler bug fixed
        if (map.mountainous.get(point)) {
            builder.append(", mountainous");
        }
//        for (fixture in map.fixtures[point]) {
        for (fixture in map.fixtures.get(point)) {
            if (unforested, is Forest fixture) {
                unforested = false;
                builder.append(", forested with ``fixture.kind``");
                fixtures.remove(fixture.id);
            } else if (is Hill fixture) {
                builder.append(", hilly");
                fixtures.remove(fixture.id);
            } else if (is Oasis fixture) {
                builder.append(", with a nearby oasis");
                fixtures.remove(fixture.id);
            }
        }
        return builder.string;
    }

    "Write HTML representing a collection of rivers."
    void riversToString(Anything(String) formatter, River* rivers) {
        if (rivers.contains(River.lake)) {
            formatter("""<li>There is a nearby lake.</li>
                         """);
        }
        value temp = rivers.filter(not(River.lake.equals));
        if (exists first = temp.first) {
            formatter("<li>There is a river on the tile, ");
            formatter("flowing through the following borders: ");
            formatter(", ".join(temp.map(River.description)));
            formatter("""</li>
                         """);
        }
    }

    "Produces a sub-report on a fortress. All fixtures referred to in this report are
     removed from the collection."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, Fortress item, Point loc) {
        ostream("<h5>Fortress ``item.name`` belonging to ");
        ostream((item.owner == currentPlayer) then "you" else item.owner.string);
        ostream("</h5>
                 <ul>
                     <li>Located at ``loc`` ``distanceString(loc)``</li>
                     <li>``terrain(map, loc, fixtures)``</li>
                     ");
        //            riversToString(ostream, *map.rivers[loc]); // TODO: syntax sugar once compiler bug fixed
        riversToString(ostream, *map.rivers.get(loc));
        if (exists roads = map.roads[loc], !roads.empty) {
            ostream("<li>There are roads going in the the following directions:");
            ostream(", ".join(roads.keys)); // TODO: Report what kinds of roads they are
            ostream("""</li>
                       """);
        }
        MutableList<IUnit> units = ArrayList<IUnit>();
        MutableList<Implement> equipment = ArrayList<Implement>();
        MutableMultimap<String, ResourcePile> resources =
                ArrayListMultimap<String, ResourcePile>();
        MutableList<FortressMember> contents = ArrayList<FortressMember>();
        for (member in item) {
            if (is IUnit member) {
                units.add(member);
            } else if (is Implement member) {
                equipment.add(member);
            } else if (is ResourcePile member) {
                resources.put(member.kind, member);
            } else {
                contents.add(member);
            }
            fixtures.remove(item.id);
        }
        void printList<Type>({Type*} list, String header,
                IReportGenerator<Type> helper) {
            if (!list.empty) {
                ostream("""<li>""");
                ostream(header);
                ostream(""":<ul>""");
                ostream(operatingSystem.newline);
                for (item in list) {
                    ostream("""<li>""");
                    helper.produceSingle(fixtures, map, ostream, item, loc);
                    ostream("""</li>""");
                    ostream(operatingSystem.newline);
                }
                ostream("""</ul></li>""");
                ostream(operatingSystem.newline);
            }
        }
        printList(units, "Units in the fortress", urg);
        printList(equipment, "Equipment", memberReportGenerator);
        if (!resources.empty) {
            ostream("""<li>Resources:<ul>
                       """);
            for (kind->list in resources.asMap) {
                printList(list, kind, memberReportGenerator);
            }
            ostream("""</ul></li>
                       """);
        }
        printList(contents, "Other fortress contents", memberReportGenerator);
        ostream("</ul>``operatingSystem.newline``");
        fixtures.remove(item.id);
    }

    "Produces a sub-report on all fortresses. All fixtures referred to in this report are
     removed from the collection."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableMap<Fortress, Point> ours = HashMap<Fortress, Point>();
        MutableMap<Fortress, Point> others = HashMap<Fortress, Point>();
        for ([loc, fort] in fixtures.items.narrow<[Point, Fortress]>()
                .sort(pairComparator)) {
            if (currentPlayer == fort.owner) {
                ours[fort] = loc;
            } else {
                others[fort] = loc;
            }
        }
        if (!ours.empty) {
            ostream("""<h4>Your fortresses in the map:</h4>
                   """);
            for (fort->loc in ours) {
                produceSingle(fixtures, map, ostream, fort, loc);
            }
        }
        if (!others.empty) {
            ostream("""<h4>Other fortresses in the map:</h4>
                   """);
            for (fort->loc in others) {
                produceSingle(fixtures, map, ostream, fort, loc);
            }
        }
    }
}
