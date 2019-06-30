import lovelace.util.common {
    DRMap=DelayedRemovalMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.explorable {
    AdventureFixture
}

"A report generator for adventure hooks."
shared class AdventureReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Point? hq = null)
        extends AbstractReportGenerator<AdventureFixture>(comp, dimensions, hq) {
    AdventureFixture->Point toEntry([Point, AdventureFixture] pair) =>
        pair.rest.first->pair.first;
    "Produce the report on all adventure hooks in the map."
    shared actual void produce(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
            Anything(String) ostream) =>
        writeMap(ostream, HeadedMapImpl<AdventureFixture, Point>(
            "<h4>Possible Adventures</h4>", null,
            fixtures.items.narrow<[Point, AdventureFixture]>().sort(pairComparator)
                .map(toEntry)), defaultFormatter(fixtures, map));

    "Produce a more verbose sub-report on an adventure hook."
    shared actual void produceSingle(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, AdventureFixture item, Point loc) {
        fixtures.remove(item.id);
        ostream("``item.briefDescription`` at ``loc``: ``item
            .fullDescription`` ``distanceString(loc)``");
        if (!item.owner.independent) {
            String player;
            if (item.owner == currentPlayer) {
                player = "you";
            } else {
                player = "another player";
            }
            ostream(" (already investigated by ``player``)");
        }
    }
}
