package drivers.generators;

import legacy.map.ILegacyMap;
import legacy.map.Point;
import legacy.map.TileType;

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.terrain.Forest;

import legacy.map.fixtures.mobile.AnimalImpl;

import java.util.Objects;

/**
 * A sample map-populator.
 */
/* package */ class SampleMapPopulator implements MapPopulator {
	/**
	 * Hares won't appear in mountains, forests, or ocean.
	 */
	@Override
	public boolean isSuitable(final ILegacyMap map, final Point location) {
		final TileType terrain = map.getBaseTerrain(location);
		return !Objects.isNull(terrain) && !map.isMountainous(location) &&
				TileType.Ocean != terrain &&
				map.getFixtures(location).stream().noneMatch(Forest.class::isInstance);
	}

	@Override
	public double getChance() {
		return 0.05;
	}

	@Override
	public void create(final Point location, final IPopulatorDriverModel model, final IDRegistrar idf) {
		model.addFixture(location, new AnimalImpl("hare", false, "wild", idf.createID()));
	}
}
