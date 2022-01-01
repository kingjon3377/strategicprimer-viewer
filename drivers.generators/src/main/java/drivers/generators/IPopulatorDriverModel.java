package drivers.generators;

import drivers.common.IDriverModel;

import common.map.Point;
import common.map.TileFixture;

public interface IPopulatorDriverModel extends IDriverModel { // TODO: Extend IMultiMapDriverModel?
	/**
	 * Add a fixture to the map.
	 */
	void addFixture(Point location, TileFixture fixture);
}
