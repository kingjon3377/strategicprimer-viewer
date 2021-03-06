import strategicprimer.model.common.map {
    IFixture,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture
}

import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
}

"Fill a new ID factory from the given map."
shared IDRegistrar createIDFactory(IMapNG|{IMapNG*}|{IFixture*} arg) {
    IDRegistrar retval = IDFactory();
    recursiveRegister(retval, arg);
    return retval;
}

void recursiveRegister(IDRegistrar factory, IMapNG|{IMapNG*}|{IFixture*} arg) {
    if (is IMapNG map = arg) {
        recursiveRegister(factory, map.fixtures.items);
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
            if (is ITownFixture fixture, exists population = fixture.population) {
                recursiveRegister(factory, population.yearlyProduction);
                recursiveRegister(factory, population.yearlyConsumption);
            }
            if (is IWorker fixture) {
                if (exists mount = fixture.mount) {
                    recursiveRegister(factory, Singleton(mount));
                }
                recursiveRegister(factory, fixture.equipment);
            }
        }
    }
}
