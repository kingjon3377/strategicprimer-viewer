import ceylon.language.meta {
    typeOf=type
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    TileFixture,
    IFixture,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Meadow,
    Shrub
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import ceylon.math.decimal {
	Decimal
}
import ceylon.logging {
	logger,
	Logger
}
"A logger."
Logger log = logger(`module strategicprimer.report`);
"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
shared class CropTabularReportGenerator
        satisfies ITableGenerator<Forest|Shrub|Meadow|Grove> {
	static String truncatedNumberString(Number<out Anything> number) {
		switch (number)
		case (is Integral<out Anything>) {
			return number.string;
		}
		case (is Float) {
			return Float.format(number, 0, 2);
		}
		case (is Decimal) {
			return truncatedNumberString(number.float);
		}
		else {
			log.warn("Unhandled Number type ``typeOf(number)`` in CropTabularReportGenerator.truncatedNumberString");
			return number.string;
		}
	}
	Point hq;
	MapDimensions dimensions;
	shared new (Point hq, MapDimensions dimensions) {
		this.hq = hq;
		this.dimensions = dimensions;
	}

    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Size", "Size Unit",
	    "Cultivation", "Status", "Crop"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";
    "Create a GUI table row representing the crop."
    shared actual [{String+}+] produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Forest|Shrub|Meadow|Grove item, Integer key, Point loc, Map<Integer, Integer> parentMap) {
        String kind;
        String cultivation;
        String status;
        String size;
        String sizeUnit;
        String crop = item.kind;
        switch (item)
        case (is Forest) {
            kind = (item.rows) then "rows" else "forest";
            cultivation = "---";
            status = "---";
            if (item.acres.positive) {
	            size = truncatedNumberString(item.acres);
	            sizeUnit = "acres";
	        } else {
	            size = "unknown";
	            sizeUnit = "---";
	        }
        }
        case (is Shrub) {
            kind = "shrub";
            cultivation = "---";
            status = "---";
            if (item.population.positive) {
	            size = item.population.string;
	            sizeUnit = "plants";
	        } else {
	            size = "unknown";
	            sizeUnit = "---";
	        }
        }
        case (is Meadow) {
            kind = (item.field) then "field" else "meadow";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = item.status.string;
            if (item.acres.positive) {
                size = truncatedNumberString(item.acres);
                sizeUnit = "acres";
            } else {
                size = "unknown";
                sizeUnit = "---";
            }
        }
        case (is Grove) {
            kind = (item.orchard) then "orchard" else "grove";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = "---";
            if (item.population.positive) {
	            size = item.population.string;
	            sizeUnit = "trees";
	        } else {
	            size = "unknown";
	            sizeUnit = "---";
	        }
        }
        fixtures.remove(key);
        return [[distanceString(loc, hq, dimensions), loc.string, kind, size, sizeUnit,
	        cultivation, status, crop]];
    }
    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Forest|Shrub|Meadow|Grove] one,
            [Point, Forest|Shrub|Meadow|Grove] two) {
        Forest|Shrub|Meadow|Grove first = one.rest.first;
        Forest|Shrub|Meadow|Grove second = two.rest.first;
        Comparison cropCmp = first.kind.compare(second.kind);
        if (cropCmp == equal) {
            Comparison cmp = DistanceComparator(hq, dimensions).compare(
                one.first, two.first);
            if (cmp == equal) {
                return comparing(byIncreasing<TileFixture, Integer>(
                            (fix) => typeOf(fix).hash), byIncreasing(TileFixture.hash))(
                    first, second);
            } else {
                return cmp;
            }
        } else {
            return cropCmp;
        }
    }
}
