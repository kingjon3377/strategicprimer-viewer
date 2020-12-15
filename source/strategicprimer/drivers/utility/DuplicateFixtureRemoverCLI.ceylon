import ceylon.language.meta {
    classDeclaration
}

import ceylon.decimal {
    decimalNumber
}

import strategicprimer.model.common.map {
    IFixture,
    HasPopulation,
    TileFixture,
    HasExtent,
    Point,
    IMapNG
}

import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Quantity,
    Implement
}

import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    AnimalImpl
}

import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Meadow,
    Shrub,
    FieldStatus
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.common {
    CLIDriver,
    emptyOptions,
    SPOptions
}

import lovelace.util.common {
    simpleMap
}

import ceylon.language.meta.model {
    ClassOrInterface
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

import lovelace.util.jvm {
    decimalize
}

"A driver to remove duplicate hills, forests, etc. from the map (to reduce the size it
 takes up on disk and the memory and CPU it takes to deal with it)."
shared class DuplicateFixtureRemoverCLI satisfies CLIDriver {
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

    "A two-parameter wrapper around [[HasExtent.combined]]."
    static Type combineExtentImpl<Type>(Type one, Type two)
        given Type satisfies HasExtent<Type> => one.combined(two);

    "A two-parameter wrapper around [[HasPopulation.combined]]."
    static Type combine<Type>(Type one, Type two)
        given Type satisfies HasPopulation<Type> => one.combined(two);

    "Combine like extents into a single object. We assume all are identical except for
     acreage."
    static Type combineExtents<Type>({Type+} list) given Type satisfies HasExtent<Type> =>
        list.rest.fold(list.first)(combineExtentImpl);

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
    shared actual UtilityDriverModel model;
    shared actual SPOptions options = emptyOptions;
    shared new (ICLIHelper cli, UtilityDriverModel model) {
        this.cli = cli;
        this.model = model;
    }

    "If [[matching]] is not [[null]], ask the user whether to remove [[fixture]], and
     return the user's answer (null on EOF). If [[matching]] is [[null]], return
     [[false]]."
    Boolean? approveRemoval(Point location, TileFixture fixture,
            TileFixture? matching) {
        if (exists matching) {
            String fCls = classDeclaration(fixture).name;
            String mCls = classDeclaration(matching).name;
            return cli.inputBooleanInSeries(
                "At ``location``: Remove '``fixture.shortDescription``', of class '``
                fCls``', ID #``fixture.id``, which matches '``
                matching.shortDescription``', of class '``mCls``', ID #``
                matching.id``?", "duplicate``fCls````mCls``");
        } else {
            return false;
        }
    }

    """"Remove" (at first we just report) duplicate fixtures (i.e. hills, forests of the
       same kind, oases, etc.---we use [[TileFixture.equalsIgnoringID]]) from every tile
       in a map."""
    void removeDuplicateFixtures(IMapNG map) {
        for (location in model.map.locations) {
            for ([deleteCallback, file, fixture, duplicates] in model.conditionallyRemoveDuplicates(location)) {
                for (duplicate in duplicates) {
                    switch (approveRemoval(location, duplicate, fixture))
                    case (true) { deleteCallback(duplicate); }
                    case (false) {}
                    case (null) { return; }
                }
            }
            coalesceResources(location);
        }
    }

    "Offer to combine like resources in a unit or fortress."
    void coalesceResources(Point location) {
        Map<ClassOrInterface<IFixture>, CoalescedHolder<out IFixture, out Object>> mapping
                = simpleMap(
            `ResourcePile`->CoalescedHolder<ResourcePile, [String, String, String,
                    Integer]>(
                (pile) => [pile.kind, pile.contents, pile.quantity.units, pile.created],
                combineResources),
            `Animal`->CoalescedHolder<Animal, [String, String, Integer]>(
                (animal) => [animal.kind, animal.status, animal.born],
                combinePopulations<Animal>),
            `Implement`->CoalescedHolder<Implement, String>(Implement.kind,
                combinePopulations<Implement>),
            `Forest`->CoalescedHolder<Forest, [String, Boolean]>(
                        (forest) => [forest.kind, forest.rows],
                combineExtents<Forest>),
            `Grove`->CoalescedHolder<Grove, [Boolean, Boolean, String]>(
                (grove) => [grove.orchard, grove.cultivated, grove.kind],
                combinePopulations<Grove>),
            `Meadow`->CoalescedHolder<Meadow, [String, Boolean, Boolean, FieldStatus]>(
                (meadow) => [meadow.kind, meadow.field, meadow.cultivated, meadow.status],
                combineExtents<Meadow>),
            `Shrub`->CoalescedHolder<Shrub, String>(Shrub.kind, combinePopulations<Shrub>)
        );

        for ([callback, context, plural, fixtures] in model.conditionallyCoalesceResources(location, mapping)) {
            cli.print(context);
            cli.println("The following ``plural`` can be combined:");
            fixtures.map(Object.string).each(cli.println);
            switch (cli.inputBooleanInSeries("Combine them? ", memberKind(fixtures.first)))
            case (true) {
                callback();
            }
            case (false) {}
            case (null) { return; }
        }
    }

    "Run the driver"
    shared actual void startDriver() {
        if (!model.subordinateMaps.empty) {
            for (map->[file, modifiedFlag] in model.allMaps) {
                removeDuplicateFixtures(map);
                if (!modifiedFlag) {
                    model.setModifiedFlag(map, true);
                }
            }
        } else {
            removeDuplicateFixtures(model.map);
            model.mapModified = true;
        }
    }
}
