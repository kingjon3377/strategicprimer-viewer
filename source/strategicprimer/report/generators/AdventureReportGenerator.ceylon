import ceylon.collection {
	ArrayList,
	MutableMap,
	MutableList
}
import lovelace.util.common {
	DRMap=DelayedRemovalMap
}

import strategicprimer.model.map {
	Point,
	IMapNG,
	invalidPoint,
	IFixture,
	Player,
	MapDimensions
}
import strategicprimer.model.map.fixtures.explorable {
	AdventureFixture
}
import strategicprimer.report {
	IReportNode
}
import strategicprimer.report.nodes {
	SimpleReportNode,
	SectionListReportNode,
	emptyReportNode
}
"A report generator for adventure hooks."
shared class AdventureReportGenerator(
		Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
		MapDimensions dimensions, Point hq = invalidPoint)
		extends AbstractReportGenerator<AdventureFixture>(comp, dimensions, hq) {
	"Produce the report on all adventure hooks in the map."
	shared actual void produce(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
			Anything(String) ostream) {
		MutableList<[Point, IFixture]> values =
				ArrayList<[Point, IFixture]> { *fixtures.items
			.sort(pairComparator) };
		HeadedMap<AdventureFixture, Point>&MutableMap<AdventureFixture, Point> adventures =
				HeadedMapImpl<AdventureFixture, Point>("<h4>Possible Adventures</h4>");
		for ([loc, item] in values) {
			if (is AdventureFixture item) {
				adventures[item] = loc;
			}
		}
		writeMap(ostream, adventures, (AdventureFixture key->Point val, formatter) => produceSingle(
				fixtures, map, formatter, key, val));
	}
	"Produce a more verbose sub-report on an adventure hook."
	shared actual void produceSingle(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
		Anything(String) ostream, AdventureFixture item, Point loc) {
		fixtures.remove(item.id);
		ostream("``item.briefDescription`` at ``loc``: ``item
			.fullDescription`` ``distCalculator.distanceString(loc)``");
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
	"Produce the report on all adventure hooks in the map."
	shared actual IReportNode produceRIR(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map) {
		MutableList<[Point, IFixture]> values =
				ArrayList<[Point, IFixture]> { *fixtures.items
			.sort(pairComparator) };
		IReportNode adventures = SectionListReportNode(4, "Possible Adventures");
		for ([loc, item] in values) {
			if (is AdventureFixture item) {
				adventures.appendNode(produceRIRSingle(fixtures, map, item, loc));
			}
		}
		if (adventures.childCount == 0) {
			return emptyReportNode;
		} else {
			return adventures;
		}
	}
	"Produce a more verbose sub-report on an adventure hook."
	shared actual IReportNode produceRIRSingle(DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
		AdventureFixture item, Point loc) {
		fixtures.remove(item.id);
		if (item.owner.independent) {
			return SimpleReportNode("``item.briefDescription`` at ``loc``: ``item
				.fullDescription`` ``distCalculator.distanceString(loc)``",
			loc);
		} else if (currentPlayer == item.owner) {
			return SimpleReportNode("``item.briefDescription`` at ``loc``: ``item
				.fullDescription`` ``distCalculator
					.distanceString(loc)`` (already investigated by you)",
			loc);
		} else {
			return SimpleReportNode("``item.briefDescription`` at ``loc``: ``item
				.fullDescription`` ``distCalculator
					.distanceString(
				loc)`` (already investigated by another player)",
			loc);
		}
	}
}