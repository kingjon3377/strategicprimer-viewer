package drivers.generators;

import common.map.IMapNG;
import common.map.Point;
import common.map.TileType;

import common.idreg.IDRegistrar;

import common.map.fixtures.terrain.Forest;

import common.map.fixtures.mobile.AnimalImpl;

/**
 * A sample map-populator.
 */
/* package */ class SampleMapPopulator implements MapPopulator {
	/**
	 * Hares won't appear in mountains, forests, or ocean.
	 */
	@Override
	public boolean isSuitable(final IMapNG map, final Point location) {
		final TileType terrain = map.getBaseTerrain(location);
		return terrain != null && !map.isMountainous(location) &&
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
