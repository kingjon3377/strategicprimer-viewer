import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap,
	matchingPredicate,
	comparingOn
}

import strategicprimer.model.map {
    Point,
    IMapNG,
    invalidPoint,
    IFixture,
    MapDimensions,
	HasPopulation,
	HasExtent
}
import strategicprimer.model.map.fixtures.resources {
    Meadow,
    CacheFixture,
    Mine,
    StoneDeposit,
    Shrub,
    MineralVein,
    Grove,
	HarvestableFixture,
	FieldStatus
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
import ceylon.decimal {
	Decimal
}
import com.vasileff.ceylon.structures {
	HashMultimap,
	MutableMultimap,
	Multimap
}
import ceylon.language.meta {
	type
}
import ceylon.logging {
	Logger,
	logger
}
import strategicprimer.model.map.fixtures.towns {
	TownStatus
}
import ceylon.whole {
    Whole
}
Logger log = logger(`module strategicprimer.report`);
"A report generator for harvestable fixtures (other than caves and battlefields, which
 aren't really)."
shared class HarvestableReportGenerator
		extends AbstractReportGenerator<HarvestableFixture> {
	static String populationCountString(HasPopulation<out Anything> item, String singular,
			String plural = singular + "s") {
		if (item.population <= 0) {
			return "";
		} else if (item.population == 1) {
			return " (1 ``singular``)";
		} else {
			return " (``item.population`` ``plural``)";
		}
	}
	static String acreageString(HasExtent item) {
		if (item.acres.positive) {
			switch (acres = item.acres)
			case (is Integral<out Anything>) {
				return " (``acres`` acres)";
			}
			case (is Float) {
				return " (``Float.format(acres, 0, 2)`` acres)";
			}
			case (is Decimal) {
				return " (``Float.format(acres.float, 0, 2)`` acres)";
			}
			else {
				if (is Integer|Whole acres) {
					log.debug("Ran into eclipse/ceylon#7382");
				} else {
					log.warn(
						"Unhandled Number type ``type(acres)`` in HarvestableReportGenerator.acreageString");
				}
				return " (``acres`` acres)";
			}
		} else {
			return "";
		}
	}
	shared new (Comparison([Point, IFixture], [Point, IFixture]) comp,
        MapDimensions dimensions, Point hq = invalidPoint)
			extends AbstractReportGenerator<HarvestableFixture>(comp, dimensions, hq) { }
    "Convert a Map from kinds to Points to a HtmlList."
	// Can't be static because HtmlList isn't and can't be
	// ("Class without parameter list may not be annotated sealed")
    HeadedList<String> mapToList(Multimap<String, Point> map, String heading) =>
            HtmlList(heading, map.asMap.filter(not(matchingPredicate(
					Iterable<Point>.empty, Entry<String, {Point*}>.item)))
		        .map((key->list) => "``key``: at ``commaSeparatedList(list)``")
				.sort(increasing));
    """Produce a sub-report(s) dealing with a single "harvestable" fixture(s). It is to be
       removed from the collection. Caves and battlefields, though HarvestableFixtures,
       are *not* handled here.""""
    shared actual void produceSingle(
			DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, HarvestableFixture item, Point loc) {
        assert (is CacheFixture|Grove|Meadow|Mine|MineralVein|Shrub|StoneDeposit item);
        switch (item)
        case (is CacheFixture) {
            ostream("At ``loc``: ``distCalculator
                .distanceString(loc)``A cache of ``item
                .kind``, containing ``item.contents``");
        }
        case (is Grove) {
            ostream("At ``loc``: ``(item.cultivated) then "cultivated" else
	            "wild"`` ``item.kind`` ``(item.orchard) then "orchard" else
	            "grove"`` ``populationCountString(item, "tree")````
	            distCalculator.distanceString(loc)``");
        }
        case (is Meadow) {
            ostream("At ``loc``: ``item.status`` ``(item.cultivated) then
	            "cultivated" else "wild or abandoned"`` ``item.kind`` ``(item
	                .field) then "field" else "meadow"`` ``acreageString(item)````
		            distCalculator.distanceString(loc)``");
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
            ostream("At ``loc``: ``item.kind`` ``populationCountString(item, "plant")````
                distCalculator.distanceString(loc)``");
        }
        case (is StoneDeposit) {
            ostream("At ``loc``: An exposed ``item
                .kind`` deposit ``distCalculator.distanceString(loc)``");
        }
    }
    """Produce the sub-report(s) dealing with "harvestable" fixtures. All fixtures
       referred to in this report are to be removed from the collection. Caves and
       battlefields, though HarvestableFixtures, are presumed to have been handled
       already.""""
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
	        IMapNG map, Anything(String) ostream) {
        MutableMultimap<String, Point> stone = HashMultimap<String, Point>();
        MutableMultimap<String, Point> shrubs = HashMultimap<String, Point>();
        MutableMultimap<String, Point> minerals = HashMultimap<String, Point>();
        MutableHeadedMap<Mine, Point> mines = HeadedMapImpl<Mine, Point>("<h5>Mines</h5>",
                comparing(byIncreasing(Mine.kind),
                    comparingOn(Mine.status, byIncreasing(TownStatus.ordinal)),
                    byIncreasing(Mine.id)));
        MutableHeadedMap<Meadow, Point> meadows = HeadedMapImpl<Meadow, Point>(
                "<h5>Meadows and Fields</h5>", comparing(byIncreasing(Meadow.kind),
                    comparingOn(Meadow.status, byIncreasing(FieldStatus.ordinal)),
                    byIncreasing(Meadow.id)));
        MutableHeadedMap<Grove, Point> groves =
				HeadedMapImpl<Grove, Point>("<h5>Groves and Orchards</h5>",
                    comparing(byIncreasing(Grove.kind), byIncreasing(Grove.id)));
        MutableHeadedMap<CacheFixture, Point> caches = HeadedMapImpl<CacheFixture, Point>(
                "<h5>Caches collected by your explorers and workers:</h5>",
                comparing(byIncreasing(CacheFixture.kind),
                    byIncreasing(CacheFixture.contents),
                    byIncreasing(CacheFixture.id)));
        for ([point, item] in fixtures.items.narrow<[Point, HarvestableFixture]>()
				.sort(pairComparator)) {
            // TODO: Use a Map by type; with reified generics we can even handle
			// differently based on whether a List or Map is in the Map!
            switch (item)
            case (is CacheFixture) {
                caches[item] = point;
                fixtures.remove(item.id);
            }
            case (is Grove) {
                groves[item] = point;
                fixtures.remove(item.id);
            }
            case (is Meadow) {
                meadows[item] = point;
                fixtures.remove(item.id);
            }
            case (is Mine) {
                mines[item] = point;
                fixtures.remove(item.id);
            }
            case (is MineralVein) {
                minerals.put(item.shortDescription, point);
                fixtures.remove(item.id);
            }
            case (is Shrub) {
                shrubs.put(item.kind, point);
                fixtures.remove(item.id);
            }
            case (is StoneDeposit) {
                stone.put(item.kind, point);
                fixtures.remove(item.id);
            }
            else {}
        }
        {HeadedList<String>+} all = [mapToList(minerals, "<h5>Mineral Deposits</h5>"),
            mapToList(stone, "<h5>Exposed Stone Deposits</h5>"),
            mapToList(shrubs, "<h5>Shrubs, Small Trees, etc.</h5>") ];
        if (!{caches, groves, meadows, mines}.every(Iterable.empty) ||
                !all.every(HeadedList.empty)) {
            ostream("""<h4>Resource Sources</h4>
                   """);
            for (HeadedMap<HarvestableFixture, Point> mapping in [caches, groves,
                    meadows, mines]) {
                writeMap(ostream, mapping, defaultFormatter(fixtures, map));
            }
            all.map(Object.string).each(ostream);
        }
    }
    """Produce a sub-report dealing with a "harvestable" fixture. All fixtures
       referred to in this report are to be removed from the collection."""
    shared actual IReportNode produceRIRSingle(
			DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, HarvestableFixture item, Point loc) {
        SimpleReportNode retval;
        assert (is CacheFixture|Grove|Meadow|Mine|MineralVein|Shrub|StoneDeposit item);
        switch (item)
        case (is CacheFixture) {
            retval = SimpleReportNode("At ``loc``: ``distCalculator
                .distanceString(loc)`` A cache of ``item.kind``, containing ``item
                .contents``", loc);
        }
        case (is Grove) {
            retval = SimpleReportNode("At ``loc``: A ``(item.cultivated) then
	            "cultivated" else "wild"`` ``item.kind`` ``(item
	                .orchard) then "orchard" else "grove"`` ``distCalculator
	                .distanceString(loc)``", loc);
        }
        case (is Meadow) {
            retval = SimpleReportNode("At ``loc``: A ``item.status`` ``(item
                .cultivated) then "cultivated" else "wild or abandoned"`` ``item
                .kind`` ``(item.field) then "field" else "meadow"`` ``distCalculator
                .distanceString(loc)``", loc);
        }
        case (is Mine) {
            retval = SimpleReportNode("At ``loc``: ``item`` ``distCalculator
                .distanceString(loc)``", loc);
        }
        case (is MineralVein) {
            retval = SimpleReportNode("At ``loc``: An ``(item
                .exposed) then "exposed" else "unexposed"`` vein of ``item
                .kind`` ``distCalculator.distanceString(loc)``", loc);
        }
        case (is Shrub) {
            retval = SimpleReportNode("At ``loc``: ``item.kind`` ``distCalculator
                .distanceString(loc)``", loc);
        }
        case (is StoneDeposit) {
            retval = SimpleReportNode("At ``loc``: An exposed ``item
                .kind`` deposit ``distCalculator.distanceString(loc)``", loc);
        }
        fixtures.remove(item.id);
        return retval;
    }
    """Produce the sub-reports dealing with "harvestable" fixture(s). All fixtures
       referred to in this report are to be removed from the collection."""
    shared actual IReportNode produceRIR(
			DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map) {
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
        for ([loc, item] in fixtures.items.narrow<[Point, HarvestableFixture]>()
				.sort(pairComparator)) {
            if (is CacheFixture item) {
                caches.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is Grove item) {
                groves.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is Meadow item) {
                meadows.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is Mine item) {
                mines.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is MineralVein item) {
                IReportNode node;
                if (exists temp = minerals[item.shortDescription]) {
                    node = temp;
                } else {
                    node = ListReportNode(item.shortDescription);
                    minerals[item.shortDescription] = node;
                }
                node.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is Shrub item) {
                IReportNode node;
                if (exists temp = shrubs[item.shortDescription]) {
                    node = temp;
                } else {
                    node = ListReportNode(item.shortDescription);
                    shrubs[item.shortDescription] = node;
                }
                node.appendNode(produceRIRSingle(fixtures, map, item, loc));
            } else if (is StoneDeposit item) {
                IReportNode node;
                if (exists temp = stone[item.kind]) {
                    node = temp;
                } else {
                    node = ListReportNode(item.kind);
                    stone[item.kind] = node;
                }
                node.appendNode(produceRIRSingle(fixtures, map, item, loc));
            }
        }
        SortedSectionListReportNode shrubsNode = SortedSectionListReportNode(5,
            "Shrubs, Small Trees, etc.");
        shrubsNode.appendNodes(*shrubs.items);
        SortedSectionListReportNode mineralsNode = SortedSectionListReportNode(5,
            "Mineral Deposits");
        mineralsNode.appendNodes(*minerals.items);
        SortedSectionListReportNode stoneNode = SortedSectionListReportNode(5,
            "Exposed Stone Deposits");
        stoneNode.appendNodes(*stone.items);
        for (node in [mines, meadows, groves, caches]) {
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
