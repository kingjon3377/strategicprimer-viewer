package legacy.idreg;

import legacy.map.Point;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.towns.ITownFixture;
import legacy.map.fixtures.FixtureIterable;

import java.util.Collections;
import java.util.Objects;

public final class IDFactoryFiller {
    private IDFactoryFiller() {
    }

    /**
     * Fill a new ID factory from the given map.
     */
    public static IDRegistrar createIDFactory(final ILegacyMap... arg) {
        final IDRegistrar retval = new IDFactory();
        for (final ILegacyMap map : arg) {
            recursiveRegister(retval, map);
        }
        return retval;
    }

    /**
     * Fill a new ID factory from the given map.
     */
    public static IDRegistrar createIDFactory(final Iterable<? extends IFixture> arg) {
        final IDRegistrar retval = new IDFactory();
        recursiveRegister(retval, arg);
        return retval;
    }

    static void recursiveRegister(final IDRegistrar factory, final ILegacyMap map) {
        for (final Point loc : map.getLocations()) {
            recursiveRegister(factory, map.getFixtures(loc));
        }
    }

    static void recursiveRegister(final IDRegistrar factory, final Iterable<? extends IFixture> arg) {
        for (final IFixture fixture : arg) {
            final int id = fixture.getId();
            if (factory.isIDUnused(id)) {
                // We don't want to set off duplicate-ID warnings for the same fixture
                // in multiple maps, so we only call register() after ensuring the ID
                // is unused
                factory.register(id);
            }
            if (fixture instanceof FixtureIterable) {
                recursiveRegister(factory, (FixtureIterable<?>) fixture);
            }
            if (fixture instanceof final ITownFixture town && !Objects.isNull(town.getPopulation())) {
                recursiveRegister(factory, town.getPopulation().getYearlyProduction());
                recursiveRegister(factory, town.getPopulation().getYearlyConsumption());
            }
            if (fixture instanceof final IWorker w) {
	            if (!Objects.isNull(w.getMount())) {
                    recursiveRegister(factory, Collections.singleton(w.getMount()));
                }
                recursiveRegister(factory, ((IWorker) fixture).getEquipment());
            }
        }
    }
}
