import ceylon.language.meta {
    typeOf=type
}

import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn
}

import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.map {
    IFixture,
    TileFixture,
    MapDimensions,
    Point,
    HasKind
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Meadow,
    Shrub
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import ceylon.decimal {
    Decimal
}
import ceylon.logging {
    logger,
    Logger
}
import ceylon.whole {
    Whole
}

"A logger."
Logger log = logger(`module strategicprimer.report`);

"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
shared class CropTabularReportGenerator
        extends AbstractTableGenerator<Forest|Shrub|Meadow|Grove>
        satisfies ITableGenerator<Forest|Shrub|Meadow|Grove> {
    "Produce a [[String]] representation of a [[Number]], limiting it to two decimal places."
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
            if (is Integer|Whole number) {
                log.debug("Ran into eclipse/ceylon#7382");
            } else {
                log.warn("Unhandled Number type ``
                    typeOf(number)`` in CropTabularReportGenerator.truncatedNumberString");
            }
            return number.string;
        }
    }

    Point hq;
    MapDimensions dimensions;
    shared new (Point hq, MapDimensions dimensions)
            extends AbstractTableGenerator<Forest|Shrub|Meadow|Grove>() {
        this.hq = hq;
        this.dimensions = dimensions;
    }

    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Size",
        "Size Unit", "Cultivation", "Status", "Crop"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";

    "Create a GUI table row representing the crop."
    shared actual [{String+}+] produce(
            DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
            Forest|Shrub|Meadow|Grove item, Integer key, Point loc,
            Map<Integer,Integer> parentMap) {
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
        return [[distanceString(loc, hq, dimensions), locationString(loc), kind, size,
            sizeUnit, cultivation, status, crop]];
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Forest|Shrub|Meadow|Grove] one,
        [Point, Forest|Shrub|Meadow|Grove] two) =>
        comparing(byIncreasing(compose(HasKind.kind, pairFixture)),
            comparingOn(pairPoint, DistanceComparator(hq, dimensions).compare),
            byIncreasing(compose(Object.hash, compose(typeOf<TileFixture>, pairFixture))),
            byIncreasing(compose(TileFixture.hash, pairFixture)))(one, two);
}
