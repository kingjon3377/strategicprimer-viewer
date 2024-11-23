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

import java.util.EnumSet;

/**
 * A factory for a driver to add some kind of fixture to suitable tiles throughout the map.
 */
@AutoService(DriverFactory.class)
public final class MapPopulatorFactory implements ModelDriverFactory<IPopulatorDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "populate-map",
			ParamCount.One, "Add missing fixtures to a map",
			"Add specified kinds of fixtures to suitable points throughout a map",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IPopulatorDriverModel model) {
		return new MapPopulatorDriver(cli, options, model);
	}

	@Override
	public IPopulatorDriverModel createModel(final IMutableLegacyMap map) {
		return new PopulatorDriverModel(map);
	}
}
