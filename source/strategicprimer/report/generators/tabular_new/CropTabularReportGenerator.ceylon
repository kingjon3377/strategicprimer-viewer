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

    "Produce a [[String]] representation of a [[Number]], limiting it to two decimal
     places." // TODO: Move to ITableGenerator or lovelace.util
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

    Point? hq;
    MapDimensions dimensions;
    Comparison(Point, Point) distanceComparator;
    shared new (Point? hq, MapDimensions dimensions)
            extends AbstractTableGenerator<Forest|Shrub|Meadow|Grove>() {
        this.hq = hq;
        this.dimensions = dimensions;
        if (exists hq) {
            distanceComparator = DistanceComparator(hq, dimensions).compare;
        } else {
            distanceComparator = (Point one, Point two) => equal;
        }
    }

    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Size",
        "Size Unit", "Cultivation", "Status", "Crop"];

    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";

    "Create a GUI table row representing the crop."
    shared actual [{[String(), Anything(String)?]+}+] produce(
            DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
            Forest|Shrub|Meadow|Grove item, Integer key, Point loc,
            Map<Integer,Integer> parentMap) {
        String() kindGetter;
        Anything(String)? kindSetter;
        String() cultivationGetter;
        Anything(String)? cultivationSetter;
        String() statusGetter;
        Anything(String)? statusSetter;
        String() sizeGetter;
        Anything(String)? sizeSetter;
        String() sizeUnitGetter;
        Anything(String)? sizeUnitSetter;
        String() cropGetter = () => item.kind;
        Anything(String) = (String str) => item.kind = str;
        switch (item)
        case (is Forest) {
            kindGetter = () => (item.rows) then "rows" else "forest";
            kindSetter = (String str) {
                if (str == "rows") {
                    item.rows = true;
                } else if (str == "forest") {
                    item.rows = false;
                } else {
                    log.warn("Invalid input for forest kind");
                }
            };
            cultivationGetter = invalidGetter;
            cultivationSetter = null;
            statusGetter = invalidGetter;
            statusSetter = null;
            sizeGetter = () => (item.acres.positive) then truncatedNumberString(item.acres) else "unknown";
            sizeSetter = (String str) => nothing; // FIXME: implement
            sizeUnitGetter = () => (item.acres.positive) then "acres" else "---";
            sizeUnitSetter = null; // TODO: Implement something here?
        }
        case (is Shrub) {
            kindGetter = () => "shrub";
            kindSetter = null;
            cultivationGetter = invalidGetter;
            cultivationSetter = null;
            statusGetter = invalidGetter;
            statusSetter = null;
            sizeGetter = () => (item.population.positive) then item.population.string else "unknown";
            sizeSetter = (String str) => nothing;
            sizeUnitGetter = () => (item.population.positive) then "plants" else "---";
            sizeUnitSetter = null;
        }
        case (is Meadow) {
            kindGetter = () => (item.field) then "field" else "meadow";
            kindSetter = (String str) => nothing;
            cultivationGetter = () => (item.cultivated) then "cultivated" else "wild";
            cultivationSetter = (String str) => nothing;
            statusGetter = () => item.status.string;
            statusSetter = (String str) => nothing;
            sizeGetter = () => (item.acres.positive) then truncatedNumberString(item.acres) else "unknown";
            sizeSetter = (String str) => nothing;
            sizeUnitGetter = () => (item.acres.positive) then "acres" else "---";
            sizeUnitSetter = null; // TODO: implement?
        }
        case (is Grove) {
            kindGetter = () => (item.orchard) then "orchard" else "grove";
            kindSetter = (String str) => nothing;
            cultivationGetter = () => (item.cultivated) then "cultivated" else "wild";
            cultivationSetter = (String str) => nothing;
            statusGetter = invalidGetter;
            statusSetter = null;
            sizeGetter = () => (item.population.positive) then item.population.string else "unknown";
            sizeSetter = (String str) => unknown;
            sizeUnitGetter = () => (item.population.positive) then "trees" else "---";
            sizeUnitSetter = null; // TODO: implement?
        }
        fixtures.remove(key);
        return [[[() => distanceString(loc, hq, dimensions), null], locationElement(key),
            [kindGetter, kindSetter], [sizeGetter, sizeSetter], [sizeUnitGetter, sizeUnitSetter],
            [cultivationGetter, cultivationSetter], [statusGetter, statusSetter], [cropGetter, cropSetter]]];
    }

    "Compare two Point-fixture pairs."
    shared actual Comparison comparePairs([Point, Forest|Shrub|Meadow|Grove] one,
            [Point, Forest|Shrub|Meadow|Grove] two) => // TODO: Should we compare kind of fixture first?
        comparing(byIncreasing(compose(HasKind.kind, pairFixture)),
            comparingOn(pairPoint, distanceComparator),
            byIncreasing(compose(Object.hash, compose(typeOf<TileFixture>, pairFixture))),
            byIncreasing(compose(TileFixture.hash, pairFixture)))(one, two);
}
