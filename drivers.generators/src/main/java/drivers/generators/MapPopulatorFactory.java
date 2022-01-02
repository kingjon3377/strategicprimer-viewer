package drivers.generators;

import common.map.IMutableMapNG;

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
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IPopulatorDriverModel) {
			return new MapPopulatorDriver(cli, options, (IPopulatorDriverModel) model);
		} else {
			return createDriver(cli, options, new PopulatorDriverModel(model));
		}
	}

	@Override
	public IPopulatorDriverModel createModel(IMutableMapNG map) {
		return new PopulatorDriverModel(map);
	}
}
