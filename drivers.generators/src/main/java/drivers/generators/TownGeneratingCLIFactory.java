package drivers.generators;

import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to let the user enter or generate 'stats' for towns.
 */
@AutoService(DriverFactory.class)
public final class TownGeneratingCLIFactory implements ModelDriverFactory<PopulationGeneratingModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "generate-towns",
			ParamCount.AtLeastOne,
			"Enter or generate stats and contents for towns and villages",
			"Enter or generate stats and contents for towns and villages",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine));

	@Override
	public final IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options,
	                                final PopulationGeneratingModel model) {
		return new TownGeneratingCLI(cli, model);
	}

	@Override
	public PopulationGeneratingModel createModel(final IMutableLegacyMap map) {
		return new PopulationGeneratingModel(map);
	}

	@Override
	public PopulationGeneratingModel createModel(final IDriverModel model) {
		return new PopulationGeneratingModel(model);
	}
}
