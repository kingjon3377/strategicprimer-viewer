import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.towns {
    Village
}

"A report generator for Villages."
shared class VillageReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Point? hq = null)
        extends AbstractReportGenerator<Village>(comp, dimensions, hq) {
    "Produce the report on all known villages."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        value villageComparator = comparing(byIncreasing(Village.name),
            byIncreasing(Village.race), byIncreasing(Village.id));
        MutableHeadedMap<Village, Point> own = HeadedMapImpl<Village, Point>(
            "<h4>Villages pledged to your service:</h4>", villageComparator);
        MutableHeadedMap<Village, Point> independents = HeadedMapImpl<Village, Point>(
            "<h4>Villages you think are independent:</h4>",
            villageComparator);
        MutableMap<Player, MutableHeadedMap<Village, Point>> others =
                HashMap<Player, MutableHeadedMap<Village, Point>>();
        for ([loc, village] in fixtures.items.narrow<[Point, Village]>()
                .sort(pairComparator)) {
            if (village.owner == currentPlayer) {
                own[village] = loc;
            } else if (village.owner.independent) {
                independents[village] = loc;
            } else {
                MutableHeadedMap<Village, Point> mapping;
                if (exists temp = others[village.owner]) {
                    mapping = temp;
                } else {
                    mapping = HeadedMapImpl<Village, Point>(
                        "<h5>Villages sworn to ``village.owner.name``</h5>
                         ", villageComparator);
                    others[village.owner] = mapping;
                }
                mapping[village] = loc;
            }
        }
        Comparison byDistance(Village->Point first, Village->Point second) =>
                distComparator(first.item, second.item);
        Anything(Village->Point, Anything(String)) writer =
                defaultFormatter(fixtures, map);
        writeMap(ostream, own, writer, byDistance);
        writeMap(ostream, independents, writer, byDistance);
        if (!others.empty) {
            ostream("""<h4>Other villages you know about:</h4>
                       """);
            for (mapping in others.items) {
                writeMap(ostream, mapping, writer, byDistance);
            }
        }
    }

    "Produce the (very brief) report for a particular village (we're probably in the
     middle of a bulleted list, but we don't assume that)."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, Village item, Point loc) {
        fixtures.remove(item.id);
        ostream("At ``loc``: ``item.name``, a(n) ``item.race`` village, ");
        if (item.owner.independent) {
            ostream("independent ");
        } else if (item.owner == currentPlayer) {
            ostream("sworn to you ");
        } else {
            ostream("sworn to ``item.owner.name`` ");
        }
        ostream(distanceString(loc));
    }
}
