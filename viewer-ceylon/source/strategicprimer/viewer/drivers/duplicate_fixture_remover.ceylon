import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}
import ceylon.language.meta {
    type,
    classDeclaration
}

import java.lang {
    JIterable=Iterable,
    JNumber=Number,
    JInteger=Integer,
    JLong=Long,
    JFloat=Float,
    JDouble=Double
}
import java.math {
    BigDecimal,
    BigInteger
}

import model.map {
    IMutableMapNG,
    TileFixture,
    IFixture,
    FixtureIterable
}
import model.map.fixtures {
    ResourcePile
}
import model.map.fixtures.mobile {
    IUnit
}
import model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Fortress
}

import util {
    Quantity
}
""""Remove" (at first we just report) duplicate fixtures (i.e. hills, forests, of the same
    kind, oases, etc.---we use [[TileFixture.equalsIgnoringID]]) from every tile in a
    map."""
void removeDuplicateFixtures(IMutableMapNG map, ICLIHelper cli) {
    Boolean approveRemoval(TileFixture fixture, TileFixture matching) {
        return cli.inputBooleanInSeries(
            "Remove '``fixture.shortDesc()``', of class '``classDeclaration(fixture)
                .name``', ID #``fixture.id``, which matches '``matching
                .shortDesc()``', of class '``classDeclaration(matching).name``', ID #``
                matching.id``?", "duplicate");
    }
    for (location in map.locations()) {
        MutableList<TileFixture> fixtures = ArrayList<TileFixture>();
        MutableList<TileFixture> toRemove = ArrayList<TileFixture>();
        if (exists ground = map.getGround(location)) {
            fixtures.add(ground);
        }
        if (exists forest = map.getForest(location)) {
            fixtures.add(forest);
        }
        for (fixture in map.getOtherFixtures(location)) {
            if (is IUnit fixture, fixture.kind.contains("TODO")) {
                continue;
            } else if (is CacheFixture fixture) {
                continue;
            }
            if (exists matching = fixtures.find((fixture.equalsIgnoringID)),
                    approveRemoval(fixture, matching)) {
                toRemove.add(fixture);
            } else {
                fixtures.add(fixture);
                if (is FixtureIterable<out Object> fixture) {
                    assert (is JIterable<out IFixture> fixture);
                    coalesceResources(fixture, cli);
                }
            }
        }
        for (fixture in toRemove) {
            map.removeFixture(location, fixture);
        }
    }
}
"Offer to combine like resources in a unit or fortress."
void coalesceResources(JIterable<out IFixture> stream, ICLIHelper cli) {
    MutableMap<[String, String, String, Integer], MutableList<ResourcePile>> resources =
            HashMap<[String, String, String, Integer], MutableList<ResourcePile>>();
    for (fixture in stream) {
        if (is FixtureIterable<out Object> fixture) {
            assert (is JIterable<out IFixture> fixture);
            coalesceResources(fixture, cli);
        } else if (is ResourcePile fixture) {
            [String, String, String, Integer] key = [fixture.kind, fixture.contents,
                fixture.quantity.units, fixture.created];
            MutableList<ResourcePile> list;
            if (exists temp = resources.get(key)) {
                list = temp;
            } else {
                list = ArrayList<ResourcePile>();
                resources.put(key, list);
            }
            list.add(fixture);
        }
    }
    if (!stream is IUnit|Fortress) {
        // We can't add items to or remove them from any other iterable
        return;
    }
    for (list in resources.items) {
        if (list.size <= 1) {
            continue;
        }
        cli.println("The following items could be combined:");
        for (item in list) {
            cli.println(item.string);
        }
        if (cli.inputBooleanInSeries("Combine them? ")) {
            ResourcePile combined = combineResources(list);
            if (is IUnit stream) {
                for (item in list) {
                    stream.removeMember(item);
                }
                stream.addMember(combined);
            } else if (is Fortress stream) {
                for (item in list) {
                    stream.removeMember(item);
                }
                stream.addMember(combined);
            }
        }
    }
}

"Combine like resources into a single resource pile. We assume that all resoruces have
 the same kind, contents, units, and created date."
ResourcePile combineResources({ResourcePile*} list) {
    assert (exists top = list.first);
    ResourcePile combined = ResourcePile(top.id, top.kind,
        top.contents, Quantity(list
            .map(ResourcePile.quantity).map(Quantity.number)
            .map(toBigDecimal).fold(BigDecimal.zero)(
                    (BigDecimal partial, BigDecimal element) => partial.add(element)),
            top.quantity.units));
    combined.created = top.created;
    return combined;
}
"Convert a Number of any known subclass to a BigDecimal."
BigDecimal toBigDecimal(JNumber number) {
    switch (number)
    case (is BigDecimal) { return number; }
    case (is BigInteger) { return BigDecimal(number); }
    case (is JInteger|JLong) { return BigDecimal.valueOf(number.longValue()); }
    case (is JFloat|JDouble) { return BigDecimal.valueOf(number.doubleValue()); }
    else { return BigDecimal(number.string); }
}