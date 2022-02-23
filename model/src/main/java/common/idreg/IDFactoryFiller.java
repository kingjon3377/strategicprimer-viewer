package common.idreg;

import common.map.Point;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.FixtureIterable;
import java.util.Collections;

public final class IDFactoryFiller {
	private IDFactoryFiller() {
	}

	/**
	 * Fill a new ID factory from the given map.
	 */
	public static IDRegistrar createIDFactory(final IMapNG... arg) {
		final IDRegistrar retval = new IDFactory();
		for (final IMapNG map : arg) {
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

	static void recursiveRegister(final IDRegistrar factory, final IMapNG map) {
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
			if (fixture instanceof ITownFixture && ((ITownFixture) fixture).getPopulation() != null) {
				recursiveRegister(factory, ((ITownFixture) fixture).getPopulation().getYearlyProduction());
				recursiveRegister(factory, ((ITownFixture) fixture).getPopulation().getYearlyConsumption());
			}
			if (fixture instanceof IWorker) {
				if (((IWorker) fixture).getMount() != null) {
					recursiveRegister(factory, Collections.singleton(((IWorker) fixture).getMount()));
				}
				recursiveRegister(factory, ((IWorker) fixture).getEquipment());
			}
		}
	}
}
