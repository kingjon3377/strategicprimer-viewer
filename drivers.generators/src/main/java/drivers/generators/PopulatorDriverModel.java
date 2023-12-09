package drivers.generators;

import drivers.common.IDriverModel;
import drivers.common.SimpleDriverModel;

import legacy.map.IMutableLegacyMap;
import legacy.map.Point;
import legacy.map.TileFixture;

public class PopulatorDriverModel extends SimpleDriverModel implements IPopulatorDriverModel {
	public PopulatorDriverModel(final IMutableLegacyMap map) {
		super(map);
	}

	// TODO: Change to private/protected and make "copyConstructor" static method?
	public PopulatorDriverModel(final IDriverModel model) {
		super(model.getRestrictedMap());
	}

	@Override
	public void addFixture(final Point location, final TileFixture fixture) {
		if (getRestrictedMap().addFixture(location, fixture)) {
			setMapModified(true);
		}
	}
}

