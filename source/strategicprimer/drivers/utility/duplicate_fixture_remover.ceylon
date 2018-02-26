import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}
import ceylon.language.meta {
    classDeclaration,
	type
}
import ceylon.math.decimal {
    decimalNumber,
    Decimal
}
import ceylon.math.whole {
    Whole
}

import java.lang {
    IllegalStateException
}
import strategicprimer.model.map {
    IFixture,
    IMutableMapNG,
    TileFixture,
	Point,
	HasPopulation,
	HasExtent
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    Quantity,
    Implement,
	numberComparator,
	UnitMember,
	FortressMember
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit,
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.map.fixtures.towns {
    Fortress
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.common {
    SPOptions,
    DriverUsage,
    IMultiMapModel,
    SimpleCLIDriver,
    ParamCount,
    IDriverUsage,
    IDriverModel,
    DriverFailedException
}
import java.io {
    IOException
}
import lovelace.util.common {
	NonNullCorrespondence
}
import ceylon.language.meta.model {
	Class
}
"""A driver to remove duplicate hills, forests, etc. from the map (to reduce the size it
   takes up on disk and the memory and CPU it takes to deal with it)."""
shared object duplicateFixtureRemoverCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-u", "--duplicates"];
        paramsWanted = ParamCount.one;
        shortDescription = "Remove duplicate fixtures";
        longDescription = "Remove duplicate fixtures (identical except ID# and on the
                           same tile) from a map.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    """"Remove" (at first we just report) duplicate fixtures (i.e. hills, forests, of the same
       kind, oases, etc.---we use [[TileFixture.equalsIgnoringID]]) from every tile in a
       map."""
    void removeDuplicateFixtures(IMutableMapNG map, ICLIHelper cli) {
        Boolean approveRemoval(Point location, TileFixture fixture, TileFixture matching) {
            String fCls = classDeclaration(fixture).name;
            String mCls = classDeclaration(matching).name;
            return cli.inputBooleanInSeries(
                "At ``location``: Remove '``fixture.shortDescription``', of class '``fCls``', ID #``
		            fixture.id``, which matches '``matching.shortDescription``', of class '``mCls
		            ``', ID #``matching.id``?", "duplicate``fCls````mCls``");
        }
        for (location in map.locations) {
            MutableList<TileFixture> fixtures = ArrayList<TileFixture>();
            MutableList<TileFixture> toRemove = ArrayList<TileFixture>();
            //        for (fixture in map.fixtures[location]) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(location)) {
                if (is IUnit fixture, fixture.kind.contains("TODO")) {
                    continue;
                } else if (is CacheFixture fixture) {
                    continue;
                } else if (is HasPopulation fixture, fixture.population > 0) {
                    continue;
                } else if (is HasExtent fixture, numberComparator.compare(0, fixture.acres) == smaller) {
                    continue;
                }
                if (exists matching = fixtures.find((fixture.equalsIgnoringID)),
                    approveRemoval(location, fixture, matching)) {
                    toRemove.add(fixture);
                } else {
                    fixtures.add(fixture);
                    if (is {IFixture*} fixture) {
                        coalesceResources("At ``location``: ", fixture, cli);
                    }
                }
            }
            for (fixture in toRemove) {
                map.removeFixture(location, fixture);
            }
        }
    }
    class CoalescedHolder<Type,Key>(Key(Type) extractor, shared Type({Type*}) combiner)
            satisfies NonNullCorrespondence<Type, MutableList<Type>>&{List<Type>*}
            given Type satisfies IFixture given Key satisfies Object {
        MutableMap<Key, MutableList<Type>> map = HashMap<Key, MutableList<Type>>();
        shared actual Boolean defines(Type key) => true;
        shared variable String plural = "unknown";
        shared actual MutableList<Type> get(Type item) {
            Key key = extractor(item);
            plural = item.plural;
            if (exists retval = map[key]) {
                return retval;
            } else {
                MutableList<Type> retval = ArrayList<Type>();
                map[key] = retval;
                return retval;
            }
        }
        shared actual Iterator<List<Type>> iterator() => map.items.iterator();
        shared void addIfType(Anything item) {
            if (is Type item) {
                get(item).add(item);
            }
        }
        shared Type combineRaw({IFixture*} list) {
            assert (is {Type*} list);
            return combiner(list);
        }
    }
    "Offer to combine like resources in a unit or fortress."
    void coalesceResources(String context, {IFixture*} stream, ICLIHelper cli) {
        Map<Class<IFixture>, CoalescedHolder<out IFixture, out Object>> mapping = map {
            `ResourcePile`->CoalescedHolder<ResourcePile, [String, String, String, Integer]>(
                (pile) => [pile.kind, pile.contents, pile.quantity.units, pile.created], combineResources),
            `Animal`->CoalescedHolder<Animal, [String, String, Integer]>(
                (animal) => [animal.kind, animal.status, animal.born], combineAnimals),
            `Implement`->CoalescedHolder<Implement, String>(Implement.kind, combineEquipment)
        };
        for (fixture in stream) {
            if (is {IFixture*} fixture) {
                String shortDesc;
                if (is TileFixture fixture) {
                    shortDesc = fixture.shortDescription;
                } else {
                    shortDesc = fixture.string;
                }
                coalesceResources(context + "In ``shortDesc``: ", fixture, cli);
            } else if (is Animal fixture) {
                if (fixture.traces || fixture.talking) {
                    continue;
                }
                if (exists handler = mapping[`Animal`]) {
                    handler.addIfType(fixture);
                }
            } else if (exists handler = mapping[type(fixture)]) {
                handler.addIfType(fixture);
            }
        }
        if (!stream is IUnit|Fortress) {
            // We can't add items to or remove them from any other iterable
            // FIXME: Take add() and remove() parameters to let us do so at the tile level
            return;
        }
        for (helper in mapping.items) {
            for (list in helper) {
                if (list.size <= 1) {
                    continue;
                }
                cli.print(context);
                cli.println("The following ``helper.plural.lowercased`` could be combined:");
                for (item in list) {
                    cli.println(item.string);
                }
                if (cli.inputBoolean("Combine them? ")) {
                    IFixture combined = helper.combineRaw(list);
                    if (is IUnit stream, is UnitMember combined, is {UnitMember*} list) {
                        for (item in list) {
                            stream.removeMember(item);
                        }
                        stream.addMember(combined);
                    } else if (is Fortress stream, is FortressMember combined, is {FortressMember*} list) {
                        for (item in list) {
                            stream.removeMember(item);
                        }
                        stream.addMember(combined);
                    }
                }
            }
        }
    }
    "Combine like [[Implement]]s into a single object. We assume that all Implements are of
     the same kind."
    Implement combineEquipment({Implement*} list) {
        assert (exists top = list.first);
        return Implement(top.kind, top.id, list.map(Implement.count).fold(0)(plus));
    }
    "Combine like Animals into a single Animal population. We assume that all animals have the
     same kind, domestication status, and turn of birth."
    Animal combineAnimals({Animal*} list) {
        assert (exists top = list.first);
        return Animal(top.kind, false, false, top.status, top.id, top.born,
            list.map(Animal.population).fold(0)(plus));
    }
    "Combine like resources into a single resource pile. We assume that all resources have
     the same kind, contents, units, and created date."
    ResourcePile combineResources({ResourcePile*} list) {
        assert (exists top = list.first);
        ResourcePile combined = ResourcePile(top.id, top.kind,
            top.contents, Quantity(list
                .map(ResourcePile.quantity).map(Quantity.number)
                    .map((num) {
                if (is Decimal num) {
                    return num;
                } else if (is Integer|Float|Whole num) {
                    return decimalNumber(num);
                } else {
                    throw IllegalStateException("Can't get here");
                }
            }).fold(decimalNumber(0))(
                (Decimal partial, Decimal element) => partial.plus(element)),
            top.quantity.units));
            combined.created = top.created;
            return combined;
        }
    "Run the driver"
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        try {
            if (is IMultiMapModel model) {
                for (pair in model.allMaps) {
                    removeDuplicateFixtures(pair.first, cli);
                }
            } else {
                removeDuplicateFixtures(model.map, cli);
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
}
