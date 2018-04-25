import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.map {
    Point,
    IFixture,
    IMapNG,
    invalidPoint,
    Player,
    MapDimensions
}
import strategicprimer.model.map.fixtures.towns {
    Village
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    SectionListReportNode,
    SectionReportNode,
    emptyReportNode
}
"A report generator for Villages."
shared class VillageReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Point hq = invalidPoint)
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
		for ([loc, village] in fixtures.items.narrow<[Point, Village]>().sort(pairComparator)) {
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
				distCalculator.compare(first.item, second.item);
		Anything(Village->Point, Anything(String)) writer =
				(Village key->Point val, Anything(String) formatter) =>
				produceSingle(fixtures, map, formatter, key, val);
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
	shared actual void produceSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
			IMapNG map, Anything(String) ostream, Village item, Point loc) {
		fixtures.remove(item.id);
		ostream("At ``loc``: ``item.name``, a(n) ``item.race`` village, ");
		if (item.owner.independent) {
			ostream("independent");
		} else if (item.owner == currentPlayer) {
			ostream("sworn to you");
		} else {
			ostream("sworn to ``item.owner.name``");
		}
		ostream(" ``distCalculator.distanceString(loc)``");
	}
	"Produce the report on all known villages."
	shared actual IReportNode produceRIR(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
			IMapNG map) {
		IReportNode own = SectionListReportNode(5,
			"Villages pledged to your service:");
		IReportNode independents =
				SectionListReportNode(5, "Villages you think are independent:");
		MutableMap<Player, IReportNode> othersMap = HashMap<Player, IReportNode>();
		for ([loc, village] in fixtures.items.narrow<[Point, Village]>().sort(pairComparator)) {
			Player owner = village.owner;
			IReportNode parent;
			if (owner == currentPlayer) {
				parent = own;
			} else if (owner.independent) {
				parent = independents;
			} else if (exists temp = othersMap[owner]) {
				parent = temp;
			} else {
				parent = SectionListReportNode(6, "Villages sworn to ``owner``");
				othersMap[owner] = parent;
			}
			parent.appendNode(produceRIRSingle(fixtures, map, village, loc));
		}
		IReportNode others = SectionListReportNode(5,
			"Other villages you know about:");
		others.addIfNonEmpty(*othersMap.items);
		IReportNode retval = SectionReportNode(4, "Villages:");
		retval.addIfNonEmpty(own, independents, others);
		if (retval.childCount == 0) {
			return emptyReportNode;
		} else {
			return retval;
		}
	}
	"Produce the (very brief) report for a particular village."
	shared actual IReportNode produceRIRSingle(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
			IMapNG map, Village item, Point loc) {
		fixtures.remove(item.id);
		if (item.owner.independent) {
			return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
				.race`` village, independent ``distCalculator.distanceString(loc)``",
			loc);
		} else if (item.owner == currentPlayer) {
			return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
				.race`` village, sworn to you ``distCalculator
					.distanceString(loc)``", loc);
		} else {
			return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
				.race`` village, sworn to ``item.owner`` ``distCalculator
					.distanceString(loc)``", loc);
		}
	}
}
