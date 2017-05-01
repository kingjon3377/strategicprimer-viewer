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
"A tabular report generator for crops---forests, groves, orchards, fields, meadows, and
 shrubs"
shared class CropTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<Forest|Shrub|Meadow|Grove> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind", "Cultivation",
        "Status", "Crop"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "crops";
    "Produce the report line for a fixture."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Forest|Shrub|Meadow|Grove item, Point loc) {
        String kind;
        String cultivation;
        String status;
        String crop = item.kind;
        switch (item)
        case (is Forest) {
            kind = (item.rows) then "rows" else "forest";
            cultivation = "---";
            status = "---";
        }
        case (is Shrub) {
            kind = "shrub";
            cultivation = "---";
            status = "---";
        }
        case (is Meadow) {
            kind = (item.field) then "field" else "meadow";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = item.status.string;
        }
        case (is Grove) {
            kind = (item.orchard) then "orchard" else "grove";
            cultivation = (item.cultivated) then "cultivated" else "wild";
            status = "---";
        }
        writeRow(ostream, distanceString(loc, hq, dimensions), loc.string, kind,
            cultivation, status, crop);
        return true;
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
