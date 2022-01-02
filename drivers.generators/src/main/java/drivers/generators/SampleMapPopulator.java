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
	public boolean isSuitable(IMapNG map, Point location) {
		TileType terrain = map.getBaseTerrain(location);
		return terrain != null && !map.isMountainous(location) && 
			!TileType.Ocean.equals(terrain) &&
			!map.getFixtures(location).stream().anyMatch(Forest.class::isInstance);
	}

	@Override
	public double getChance() {
		return 0.05;
	}

	@Override
	public void create(Point location, IPopulatorDriverModel model, IDRegistrar idf) {
		model.addFixture(location, new AnimalImpl("hare", false, "wild", idf.createID()));
	}
}
