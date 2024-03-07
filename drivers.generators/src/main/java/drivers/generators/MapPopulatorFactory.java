package drivers.generators;

import legacy.map.IMutableLegacyMap;

import drivers.common.DriverFactory;
import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.ModelDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.ParamCount;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to add some kind of fixture to suitable tiles throughout the map.
 */
@AutoService(DriverFactory.class)
public class MapPopulatorFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "populate-map", ParamCount.One,
			"Add missing fixtures to a map",
			"Add specified kinds of fixtures to suitable points throughout a map", true, false,
			"--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final IPopulatorDriverModel pdm) {
			return new MapPopulatorDriver(cli, options, pdm);
		} else {
			return createDriver(cli, options, new PopulatorDriverModel(model));
		}
	}

	@Override
	public IPopulatorDriverModel createModel(final IMutableLegacyMap map) {
		return new PopulatorDriverModel(map);
	}
}
