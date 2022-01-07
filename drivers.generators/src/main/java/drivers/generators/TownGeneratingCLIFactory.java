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
 * A factory for a driver to let the user enter or generate 'stats' for towns.
 */
@AutoService(DriverFactory.class)
public class TownGeneratingCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "generate-towns",
		ParamCount.AtLeastOne,
		"Enter or generate stats and contents for towns and villages",
		"Enter or generate stats and contents for towns and villages", true, false);
	
	@Override
	public final IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof PopulationGeneratingModel) {
			return new TownGeneratingCLI(cli, (PopulationGeneratingModel) model);
		} else {
			return createDriver(cli, options, new PopulationGeneratingModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new PopulationGeneratingModel(map);
	}
}
