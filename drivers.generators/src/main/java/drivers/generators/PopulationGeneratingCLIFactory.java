package drivers.generators;

import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to let the user generate animal and shrub
 * populations, meadow and grove sizes, and forest acreages.
 */
@AutoService(DriverFactory.class)
public final class PopulationGeneratingCLIFactory implements ModelDriverFactory<PopulationGeneratingModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine,
			"generate-populations", ParamCount.One, "Generate animal populations, etc.",
			"Generate animal and shrub populations, meadow and grove sizes, and forest acreages.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine)); // TODO: We'd like a GUI equivalent

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options,
	                                final PopulationGeneratingModel model) {
		return new PopulationGeneratingCLI(cli, model);
	}

	@Override
	public PopulationGeneratingModel createModel(final IMutableLegacyMap map) {
		return new PopulationGeneratingModel(map);
	}
}
