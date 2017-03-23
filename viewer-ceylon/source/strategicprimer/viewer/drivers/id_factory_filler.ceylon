import controller.map.misc {
    IDRegistrar,
    IDFactory
}
import strategicprimer.viewer.model {
    IMultiMapModel
}
import model.map {
    IFixture,
    IMapNG,
    FixtureIterable
}
import model.map.fixtures {
    Ground
}
import model.map.fixtures.terrain {
    Forest
}
import java.lang {
    JIterable=Iterable
}
import ceylon.interop.java {
    CeylonIterable
}
"Fill a new ID factory from the given map."
shared IDRegistrar createIDFactory(IMapNG|IMultiMapModel|{IFixture*} arg) {
    IDRegistrar retval = IDFactory();
    recursiveRegister(retval, arg);
    return retval;
}

void recursiveRegister(IDRegistrar factory, IMapNG|IMultiMapModel|{IFixture*} arg) {
    if (is IMapNG map = arg) {
        for (location in map.locations()) {
            Ground? tempGround = map.getGround(location);
            Forest? tempForest = map.getForest(location);
            recursiveRegister(factory, {tempGround, tempForest,
                   *map.getOtherFixtures(location)}.coalesced);
        }
    } else if (is IMultiMapModel model = arg) {
        for (pair in model.allMaps) {
            recursiveRegister(factory, pair.first);
        }
    } else if (is {IFixture*} arg) {
        for (fixture in arg) {
            Integer id = fixture.id;
            if (factory.isIDUnused(id)) {
                // We don't want to set off duplicate-ID warnings for the same fixture
                // in multiple maps, so we only call register() after ensuring the ID
                // is unused
                factory.register(id);
            }
            if (is FixtureIterable<out Object> fixture) {
                assert (is JIterable<out IFixture> fixture);
                recursiveRegister(factory, CeylonIterable(fixture));
            }
        }
    }
}