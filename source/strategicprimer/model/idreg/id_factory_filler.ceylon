import strategicprimer.model.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.map {
    IMap,
    IFixture
}
"Fill a new ID factory from the given map."
shared IDRegistrar createIDFactory(IMap|{IMap*}|{IFixture*} arg) {
    IDRegistrar retval = IDFactory();
    recursiveRegister(retval, arg);
    return retval;
}

void recursiveRegister(IDRegistrar factory, IMap|{IMap*}|{IFixture*} arg) {
    if (is IMap map = arg) {
        for (location in map.locations) {
            recursiveRegister(factory, map.allFixtures(location));
        }
    } else if (is {IMap*} model = arg) {
        for (map in model) {
            recursiveRegister(factory, map);
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
            if (is {IFixture*} fixture) {
                recursiveRegister(factory, fixture);
            }
        }
    }
}