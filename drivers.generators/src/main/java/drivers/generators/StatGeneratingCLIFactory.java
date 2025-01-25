package drivers.generators;

import drivers.common.IDriverModel;
import legacy.map.IMutableLegacyMap;

import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to generate new workers.
 */
@AutoService(DriverFactory.class)
public final class StatGeneratingCLIFactory implements ModelDriverFactory<PopulationGeneratingModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "generate-stats",
			ParamCount.AtLeastOne, "Generate new workers.",
			"Generate new workers with random stats and experience.", EnumSet.of(IDriverUsage.DriverMode.CommandLine),
			"--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options,
	                                final PopulationGeneratingModel model) {
		return new StatGeneratingCLI(cli, model);
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
