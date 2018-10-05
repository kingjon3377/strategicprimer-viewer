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
import ceylon.decimal {
    decimalNumber,
    Decimal
}
import ceylon.whole {
    Whole
}

import strategicprimer.model.common.map {
    IFixture,
    HasPopulation,
    TileFixture,
    HasExtent,
    Point,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity,
    Implement
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal,
    AnimalImpl
}
import strategicprimer.model.common.map.fixtures.resources {
    CacheFixture,
    Grove,
    Meadow,
    Shrub,
    FieldStatus
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.common {
    SPOptions,
    DriverUsage,
    IMultiMapModel,
    IDriverUsage,
    IDriverModel,
    CLIDriver,
    ParamCount,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import lovelace.util.common {
    NonNullCorrespondence,
    simpleMap,
    PathWrapper
}
import ceylon.language.meta.model {
    ClassOrInterface
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

"A factory for a driver to remove duplicate hills, forests, etc., from the map (to reduce
 the disk space it takes up and the memory and CPU required to deal with it)."
service(`interface DriverFactory`)
shared class DuplicateFixtureRemoverFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-u", "--duplicates"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Remove duplicate fixtures";
        longDescription = "Remove duplicate fixtures (identical except ID# and on the
                           same tile) from a map.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => DuplicateFixtureRemoverCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}
"A driver to remove duplicate hills, forests, etc. from the map (to reduce the size it
 takes up on disk and the memory and CPU it takes to deal with it)."
shared class DuplicateFixtureRemoverCLI satisfies CLIDriver {
    static void ifApplicable<Desired, Provided>(Anything(Desired) func)(Provided item) {
        if (is Desired item) {
            func(item);
        }
    }
    static class CoalescedHolder<Type,Key>(Key(Type) extractor, shared Type({Type+}) combiner)
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
        shared Type combineRaw({IFixture+} list) {
            assert (is {Type+} list);
            return combiner(list);
        }
    }
    static String memberKind(IFixture? member) {
        switch (member)
        case (is AnimalImpl|Implement|Forest|Grove|Meadow) {
            return member.kind;
        }
        case (is ResourcePile) {
            return member.contents;
        }
        case (null) {
            return "null";
        }
        else {
            return member.string;
        }
    }
    static Decimal decimalize(Number<out Anything> num) {
        assert (is Decimal|Whole|Integer|Float num);
        switch (num)
        case (is Decimal) {
            return num;
        }
        case (is Whole|Integer|Float) {
            return decimalNumber(num);
        }
    }
    "Combine like [[Forest]]s into a single object. We assume that all Forests are of the
     same kind of tree and either all or none are in rows."
    static Forest combineForests({Forest*} list) {
        assert (exists top = list.first);
        return Forest(top.kind, top.rows, top.id,
            list.map(Forest.acres).map(decimalize).fold(decimalNumber(0))(plus));
    }
    "Combine like [[Meadow]]s into a single object. We assume all Meadows are identical
     except for acreage and ID."
    static Meadow combineMeadows({Meadow*} list) {
        assert (exists top = list.first);
        return Meadow(top.kind, top.field, top.cultivated, top.id, top.status,
            list.map(Meadow.acres).map(decimalize).fold(decimalNumber(0))(plus));
    }
    "A two-parameter wrapper around [[HasPopulation.combined]]."
    static Type combine<Type>(Type one, Type two) given Type satisfies HasPopulation<Type> =>
            one.combined(two);
    "Combine like populations into a single object. We assume all are identical (i.e. of
     the same kind, and in the case of animals have the same domestication status and
     turn of birth) except for population."
    static Type combinePopulations<Type>({Type+} list)
            given Type satisfies HasPopulation<Type> =>
            list.rest.fold(list.first)(combine);
    "Combine like resources into a single resource pile. We assume that all resources have
     the same kind, contents, units, and created date."
    static ResourcePile combineResources({ResourcePile*} list) {
        assert (exists top = list.first);
        ResourcePile combined = ResourcePile(top.id, top.kind,
            top.contents, Quantity(list
                .map(ResourcePile.quantity).map(Quantity.number)
                .map(decimalize).fold(decimalNumber(0))(plus),
                top.quantity.units));
        combined.created = top.created;
        return combined;
    }
    ICLIHelper cli;
    shared actual IDriverModel model;
    shared new (ICLIHelper cli, IDriverModel model) {
        this.cli = cli;
        this.model = model;
    }
    Boolean approveRemoval(Point location, TileFixture fixture,
            TileFixture matching) {
        String fCls = classDeclaration(fixture).name;
        String mCls = classDeclaration(matching).name;
        return cli.inputBooleanInSeries(
            "At ``location``: Remove '``fixture.shortDescription``', of class '``
            fCls``', ID #``fixture.id``, which matches '``
            matching.shortDescription``', of class '``mCls``', ID #``
            matching.id``?", "duplicate``fCls````mCls``");
    }
    """"Remove" (at first we just report) duplicate fixtures (i.e. hills, forests of the
       same kind, oases, etc.---we use [[TileFixture.equalsIgnoringID]]) from every tile
       in a map."""
    void removeDuplicateFixtures(IMutableMapNG map) {
        for (location in map.locations) {
            MutableList<TileFixture> fixtures = ArrayList<TileFixture>();
            MutableList<TileFixture> toRemove = ArrayList<TileFixture>();
            String context = "At ``location``: ";
            //        for (fixture in map.fixtures[location]) { // TODO: syntax sugar once compiler bug fixed
            for (fixture in map.fixtures.get(location)) {
                if (is IUnit fixture, fixture.kind.contains("TODO")) {
                    continue;
                } else if (is CacheFixture fixture) {
                    continue;
                } else if (is HasPopulation<out Anything> fixture,
                        fixture.population.positive) {
                    continue;
                } else if (is HasExtent fixture, fixture.acres.positive) {
                    continue;
                }
                if (exists matching = fixtures.find(fixture.equalsIgnoringID),
                        approveRemoval(location, fixture, matching)) {
                    toRemove.add(fixture);
                } else {
                    fixtures.add(fixture);
                    if (is IUnit fixture) {
                        coalesceResources(context, fixture,
                            ifApplicable(fixture.addMember),
                            ifApplicable(fixture.removeMember));
                    } else if (is Fortress fixture) {
                        coalesceResources(context, fixture,
                            ifApplicable(fixture.addMember),
                            ifApplicable(fixture.removeMember));
                    }
                }
            }
            for (fixture in toRemove) {
                map.removeFixture(location, fixture);
            }
            coalesceResources(context, map.fixtures.get(location),
                ifApplicable<TileFixture, IFixture>(shuffle(curry(map.addFixture))),
                ifApplicable<TileFixture, IFixture>(shuffle(curry(map.removeFixture))));
        }
    }
    "Offer to combine like resources in a unit or fortress."
    void coalesceResources(String context, {IFixture*} stream,
            Anything(IFixture) add, Anything(IFixture) remove) {
        Map<ClassOrInterface<IFixture>, CoalescedHolder<out IFixture, out Object>> mapping
                = simpleMap(
            `ResourcePile`->CoalescedHolder<ResourcePile, [String, String, String, Integer]>(
                (pile) => [pile.kind, pile.contents, pile.quantity.units, pile.created],
                combineResources),
            `Animal`->CoalescedHolder<Animal, [String, String, Integer]>(
                (animal) => [animal.kind, animal.status, animal.born],
                combinePopulations<Animal>),
            `Implement`->CoalescedHolder<Implement, String>(Implement.kind,
                combinePopulations<Implement>),
            `Forest`->CoalescedHolder<Forest, [String, Boolean]>(
                        (forest) => [forest.kind, forest.rows],
                combineForests),
            `Grove`->CoalescedHolder<Grove, [Boolean, Boolean, String]>(
                (grove) => [grove.orchard, grove.cultivated, grove.kind],
                combinePopulations<Grove>),
            `Meadow`->CoalescedHolder<Meadow, [String, Boolean, Boolean, FieldStatus]>(
                (meadow) => [meadow.kind, meadow.field, meadow.cultivated, meadow.status],
                combineMeadows),
            `Shrub`->CoalescedHolder<Shrub, String>(Shrub.kind, combinePopulations<Shrub>)
        );
        for (fixture in stream) {
            if (is {IFixture*} fixture) {
                String shortDesc;
                if (is TileFixture fixture) {
                    shortDesc = fixture.shortDescription;
                } else {
                    shortDesc = fixture.string;
                }
                if (is IUnit fixture) {
                    coalesceResources(context + "In ``shortDesc``: ", fixture,
                        ifApplicable(fixture.addMember),
                        ifApplicable(fixture.removeMember));
                } else if (is Fortress fixture) {
                    coalesceResources(context + "In ``shortDesc``: ", fixture,
                        ifApplicable(fixture.addMember),
                        ifApplicable(fixture.removeMember));
                }
            } else if (is Animal fixture) {
                if (fixture.talking) {
                    continue;
                }
                if (exists handler = mapping[`Animal`]) {
                    handler.addIfType(fixture);
                }
            } else if (is HasPopulation<out Anything> fixture, fixture.population < 0) {
                continue;
            } else if (is HasExtent fixture, !fixture.acres.positive) {
                continue;
            } else if (exists handler = mapping[type(fixture)]) {
                handler.addIfType(fixture);
            }
        }
        for (helper in mapping.items) {
            for (list in helper.map(shuffle(List<IFixture>.sequence)())) {
                if (list.size <= 1) {
                    continue;
                }
                assert (nonempty list);
                cli.print(context);
                cli.println(
                    "The following ``helper.plural.lowercased`` can be combined:");
                list.map(Object.string).each(cli.println);
                if (cli.inputBooleanInSeries("Combine them? ", memberKind(list.first))) {
                    IFixture combined = helper.combineRaw(list);
                    list.each(remove);
                    add(combined);
                }
            }
        }
    }
    "Run the driver"
    shared actual void startDriver() {
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.allMaps) {
                removeDuplicateFixtures(map);
                model.setModifiedFlag(map, true);
            }
        } else {
            removeDuplicateFixtures(model.map);
            model.mapModified = true;
        }
    }
}
