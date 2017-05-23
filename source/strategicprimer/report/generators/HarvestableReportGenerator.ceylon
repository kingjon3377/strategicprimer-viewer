import ceylon.collection {
    ArrayList,
    MutableMap,
    MutableList,
    HashMap
}

import java.lang {
    IllegalArgumentException
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.map {
    Point,
    IMapNG,
    invalidPoint,
    IFixture,
    MapDimensions
}
import strategicprimer.model.map.fixtures.resources {
    HarvestableFixture,
    Meadow,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub,
    MineralVein,
    Grove
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    SortedSectionListReportNode,
    ListReportNode,
    SectionReportNode,
    emptyReportNode
}
"A report generator for harvestable fixtures (other than caves and battlefields, which
 aren't really)."
shared class HarvestableReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp,
        MapDimensions dimensions, Point hq = invalidPoint)
        extends AbstractReportGenerator<HarvestableFixture>(comp, dimensions, hq) {
    "Convert a Map from kinds to Points to a HtmlList."
    HeadedList<String>&MutableList<String> mapToList(Map<String, MutableList<Point>> map,
            String heading) =>
            HtmlList(heading, map.items.map(Object.string).sort(increasing));
    """Produce the sub-report(s) dealing with "harvestable" fixture(s). All fixtures
       referred to in this report are to be removed from the collection. Caves and
       battlefields, though HarvestableFixtures, are presumed to have been handled
       already.""""
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [HarvestableFixture, Point]? entry) {
        if (exists entry) {
            Point loc = entry.rest.first;
            switch (item = entry.first)
            case (is CacheFixture) {
                ostream("At ``loc``: ``distCalculator
                    .distanceString(loc)``A cache of ``item
                    .kind``, containing ``item.contents``");
            }
            case (is Grove) {
                ostream("At ``loc``: ``(item.cultivated) then "cultivated" else
                "wild"`` ``item.kind`` ``(item.orchard) then "orchard" else
                "grove"`` ``distCalculator.distanceString(loc)``");
            }
            case (is Meadow) {
                ostream("At ``loc``: ``item.status`` ``(item.cultivated) then
                "cultivated" else "wild or abandoned"`` ``item.kind`` ``(item
                    .field) then "field" else "meadow"`` ``distCalculator
                    .distanceString(loc)``");
            }
            case (is Mine) {
                ostream("At ``loc``: ``item`` ``distCalculator
                    .distanceString(loc)``");
            }
            case (is MineralVein) {
                ostream("At ``loc``: An ``(item.exposed) then
                "exposed" else "unexposed"`` vein of ``item
                    .kind`` ``distCalculator.distanceString(loc)``");
            }
            case (is Shrub) {
                ostream("At ``loc``: ``item.kind`` ``distCalculator
                    .distanceString(loc)``");
            }
            case (is StoneDeposit) {
                ostream("At ``loc``: An exposed ``item
                    .kind`` deposit ``distCalculator.distanceString(loc)``");
            }
            else {
                throw IllegalArgumentException(
                    "Unexpected HarvestableFixture type");
            }
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableMap<String, MutableList<Point>> stone =
                    HashMap<String, MutableList<Point>>();
            MutableMap<String, MutableList<Point>> shrubs =
                    HashMap<String, MutableList<Point>>();
            MutableMap<String, MutableList<Point>> minerals =
                    HashMap<String, MutableList<Point>>();
            HeadedMap<Mine, Point>&MutableMap<Mine, Point> mines =
                    HeadedMapImpl<Mine, Point>("<h5>Mines</h5>",
                        comparing(byIncreasing(Mine.kind),
                            byIncreasing((Mine mine) => mine.status.ordinal),
                            byIncreasing(Mine.id)));
            HeadedMap<Meadow, Point>&MutableMap<Meadow, Point> meadows =
                    HeadedMapImpl<Meadow, Point>(
                        "<h5>Meadows and Fields</h5>",
                        comparing(byIncreasing(Meadow.kind),
                            byIncreasing((Meadow meadow) => meadow.status.ordinal),
                            byIncreasing(Meadow.id)));
            HeadedMap<Grove, Point>&MutableMap<Grove, Point> groves =
                    HeadedMapImpl<Grove, Point>("<h5>Groves and Orchards</h5>",
                        comparing(byIncreasing(Grove.kind), byIncreasing(Grove.id)));
            HeadedMap<CacheFixture, Point>&MutableMap<CacheFixture, Point> caches =
                    HeadedMapImpl<CacheFixture, Point>(
                        "<h5>Caches collected by your explorers and workers:</h5>",
                        comparing(byIncreasing(CacheFixture.kind),
                            byIncreasing(CacheFixture.contents),
                            byIncreasing(CacheFixture.id)));
            for ([point, item] in values) {
                // TODO: Use a Map by type (or at least a switch); now we have reified
                // generics we can even handle differently based on whether a List or Map
                // is in the Map!
                if (is CacheFixture item) {
                    caches.put(item, point);
                } else if (is Grove item) {
                    groves.put(item, point);
                } else if (is Meadow item) {
                    meadows.put(item, point);
                } else if (is Mine item) {
                    mines.put(item, point);
                } else if (is MineralVein item) {
                    if (exists coll = minerals.get(item.shortDescription)) {
                        coll.add(point);
                    } else {
                        value coll = PointList("``item.shortDescription``: at ");
                        minerals.put(item.shortDescription, coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                } else if (is Shrub item) {
                    if (exists coll = shrubs.get(item.kind)) {
                        coll.add(point);
                    } else {
                        value coll = PointList("``item.kind``: at ");
                        shrubs.put(item.kind, coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                } else if (is StoneDeposit item) {
                    if (exists coll = stone.get(item.kind)) {
                        coll.add(point);
                    } else {
                        value coll = PointList("``item.kind``: at ");
                        stone.put(item.kind, coll);
                        coll.add(point);
                    }
                    fixtures.remove(item.id);
                }
            }
            {HeadedList<String>+} all = {mapToList(minerals, "<h5>Mineral Deposits</h5>"),
                mapToList(stone, "<h5>Exposed Stone Deposits</h5>"),
                mapToList(shrubs, "<h5>Shrubs, Small Trees, etc.</h5>") };
            if (!{caches, groves, meadows, mines}.every(Iterable.empty) ||
                    !all.every(HeadedList.empty)) {
                ostream("""<h4>Resource Sources</h4>
                       """);
                for (HeadedMap<HarvestableFixture, Point> mapping in {caches, groves,
                        meadows, mines}) {
                    writeMap(ostream, mapping,
                                (HarvestableFixture->Point entry,
                                    Anything(String) formatter) => produce(fixtures, map,
                                    formatter, [entry.key, entry.item]));
                }
                for (list in all) {
                    ostream(list.string);
                }
            }
        }
    }
    """Produce the sub-report(s) dealing with "harvestable" fixture(s). All fixtures
       referred to in this report are to be removed from the collection."""
    shared actual IReportNode produceRIR(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [HarvestableFixture, Point]? entry) {
        if (exists entry) {
            HarvestableFixture item = entry.first;
            Point loc = entry.rest.first;
            SimpleReportNode retval;
            if (is CacheFixture item) {
                retval = SimpleReportNode("At ``loc``: ``distCalculator
                    .distanceString(loc)`` A cache of ``item.kind``, containing ``item
                    .contents``", loc);
            } else if (is Grove item) {
                retval = SimpleReportNode("At ``loc``: A ``(item.cultivated) then
                "cultivated" else "wild"`` ``item.kind`` ``(item
                    .orchard) then "orchard" else "grove"`` ``distCalculator
                    .distanceString(loc)``", loc);
            } else if (is Meadow item) {
                retval = SimpleReportNode("At ``loc``: A ``item.status`` ``(item
                    .cultivated) then "cultivated" else "wild or abandoned"`` ``item
                    .kind`` ``(item.field) then "field" else "meadow"`` ``distCalculator
                    .distanceString(loc)``", loc);
            } else if (is Mine item) {
                retval = SimpleReportNode("At ``loc``: ``item`` ``distCalculator
                    .distanceString(loc)``", loc);
            } else if (is MineralVein item) {
                retval = SimpleReportNode("At ``loc``: An ``(item
                    .exposed) then "exposed" else "unexposed"`` vein of ``item
                    .kind`` ``distCalculator.distanceString(loc)``", loc);
            } else if (is Shrub item) {
                retval = SimpleReportNode("At ``loc``: ``item.kind`` ``distCalculator
                    .distanceString(loc)``", loc);
            } else if (is StoneDeposit item) {
                retval = SimpleReportNode("At ``loc``: An exposed ``item
                    .kind`` deposit ``distCalculator.distanceString(loc)``", loc);
            } else {
                throw IllegalArgumentException("Unexpected HarvestableFixture type");
            }
            fixtures.remove(item.id);
            return retval;
        } else {
            value values = fixtures.items.sort(pairComparator);
            MutableMap<String, IReportNode> stone = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> shrubs = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> minerals = HashMap<String, IReportNode>();
            SortedSectionListReportNode mines = SortedSectionListReportNode(5, "Mines");
            SortedSectionListReportNode meadows =
                    SortedSectionListReportNode(5, "Meadows and Fields");
            SortedSectionListReportNode groves =
                    SortedSectionListReportNode(5, "Groves and Orchards");
            SortedSectionListReportNode caches = SortedSectionListReportNode(5,
                "Caches collected by your explorers and workers:");
            mines.suspend();
            meadows.suspend();
            groves.suspend();
            caches.suspend();
            for ([loc, item] in values) {
                if (is HarvestableFixture item) {
                    if (is CacheFixture item) {
                        caches.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is Grove item) {
                        groves.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is Meadow item) {
                        meadows.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is Mine item) {
                        mines.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is MineralVein item) {
                        IReportNode node;
                        if (exists temp = minerals.get(item.shortDescription)) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.shortDescription);
                            minerals.put(item.shortDescription, node);
                        }
                        node.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is Shrub item) {
                        IReportNode node;
                        if (exists temp = shrubs.get(item.shortDescription)) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.shortDescription);
                            shrubs.put(item.shortDescription, node);
                        }
                        node.appendNode(produceRIR(fixtures, map, [item, loc]));
                    } else if (is StoneDeposit item) {
                        IReportNode node;
                        if (exists temp = stone.get(item.kind)) {
                            node = temp;
                        } else {
                            node = ListReportNode(item.kind);
                            stone.put(item.kind, node);
                        }
                        node.appendNode(produceRIR(fixtures, map, [item, loc]));
                    }
                }
            }
            SortedSectionListReportNode shrubsNode = SortedSectionListReportNode(5,
                "Shrubs, Small Trees, etc.");
            shrubsNode.suspend();
            for (node in shrubs.items) {
                shrubsNode.appendNode(node);
            }
            SortedSectionListReportNode mineralsNode = SortedSectionListReportNode(5,
                "Mineral Deposits");
            mineralsNode.suspend();
            for (node in minerals.items) {
                mineralsNode.appendNode(node);
            }
            SortedSectionListReportNode stoneNode = SortedSectionListReportNode(5,
                "Exposed Stone Deposits");
            stoneNode.suspend();
            for (node in stone.items) {
                stoneNode.appendNode(node);
            }
            for (node in {shrubsNode, mineralsNode, stoneNode, mines, meadows, groves,
                    caches}) {
                node.resume();
            }
            SectionReportNode retval = SectionReportNode(4, "Resource Sources");
            retval.addIfNonEmpty(caches, groves, meadows, mines, mineralsNode, stoneNode,
                shrubsNode);
            if (retval.childCount == 0) {
                return emptyReportNode;
            } else {
                return retval;
            }
        }
    }
}
