import strategicprimer.model.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.map {
    IMapNG,
    IFixture
}
"Fill a new ID factory from the given map."
shared IDRegistrar createIDFactory(IMapNG|{IMapNG*}|{IFixture*} arg) {
    IDRegistrar retval = IDFactory();
    recursiveRegister(retval, arg);
    return retval;
}

void recursiveRegister(IDRegistrar factory, IMapNG|{IMapNG*}|{IFixture*} arg) {
    if (is IMapNG map = arg) {
        for (location in map.locations) {
//            recursiveRegister(factory, map.fixtures[location]); // TODO: syntax sugar once compiler bug fixed
            recursiveRegister(factory, map.fixtures.get(location));
        }
    } else if (is {IMapNG*} model = arg) {
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