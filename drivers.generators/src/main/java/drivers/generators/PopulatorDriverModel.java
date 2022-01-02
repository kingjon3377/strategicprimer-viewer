package drivers.generators;

import drivers.common.IDriverModel;
import drivers.common.SimpleDriverModel;

import common.map.IMutableMapNG;
import common.map.Point;
import common.map.TileFixture;

public class PopulatorDriverModel extends SimpleDriverModel implements IPopulatorDriverModel {
	public PopulatorDriverModel(IMutableMapNG map) {
		super(map);
	}

	// TODO: Change to private/protected and make "copyConstructor" static method?
	public PopulatorDriverModel(IDriverModel model) {
		super(model.getRestrictedMap());
	}

	@Override
	public void addFixture(Point location, TileFixture fixture) {
		if (getRestrictedMap().addFixture(location, fixture)) {
			setMapModified(true);
		}
	}
}

