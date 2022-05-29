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

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to let the user generate animal and shrub
 * populations, meadow and grove sizes, and forest acreages.
 */
@AutoService(DriverFactory.class)
public class PopulationGeneratingCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "generate-populations",
		ParamCount.One, "Generate animal populations, etc.",
		"Generate animal and shrub populations, meadow and grove sizes, and forest acreages.", true,
		false); // TODO: We'd like a GUI equivalent

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof PopulationGeneratingModel pgm) {
			return new PopulationGeneratingCLI(cli, pgm);
		} else {
			return createDriver(cli, options, new PopulationGeneratingModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new PopulationGeneratingModel(map);
	}
}
