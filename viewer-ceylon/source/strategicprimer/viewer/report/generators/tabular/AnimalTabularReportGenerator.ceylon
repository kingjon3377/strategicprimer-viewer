import lovelace.util.common {
    DelayedRemovalMap
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    IFixture
}
import model.map {
    Point
}
"A report generator for sightings of animals."
shared class AnimalTabularReportGenerator(Point hq) satisfies ITableGenerator<Animal> {
    "The header row for the table."
    shared actual [String+] headerRow = ["Distance", "Location", "Kind"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "animals";
    "Produce a single line of the tabular report on animals."
    shared actual Boolean produce(Anything(String) ostream,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            Animal item, Point loc) {
        String kind;
        if (item.traces) {
            kind = "tracks or traces of ``item.kind``";
        } else if (item.talking) {
            kind = "talking ``item.kind``";
        } else if ("wild" != item.status) {
            kind = "``item.status`` ``item.kind``";
        } else {
            kind = item.kind;
        }
        writeRow(ostream, distanceString(loc, hq), loc.string, kind);
        return true;
    }
    "Compare two pairs of Animals and locations."
    shared actual Comparison comparePairs([Point, Animal] one, [Point, Animal] two) {
        Comparison cmp = DistanceComparator(hq).compare(one.first, two.first);
        if (cmp == equal) {
            Comparison(Animal, Animal) compareBools(Boolean(Animal) func) {
                Comparison retval(Boolean first, Boolean second) {
                    if (first == second) {
                        return equal;
                    } else if (first) {
                        return larger;
                    } else {
                        return smaller;
                    }
                }
                return (Animal first, Animal second) => retval(func(first), func(second));
            }
            return comparing(compareBools(Animal.talking),
                compareBools((animal) => !animal.traces), byIncreasing(Animal.kind))(
                one.rest.first, two.rest.first);
        } else {
            return cmp;
        }
    }
}
