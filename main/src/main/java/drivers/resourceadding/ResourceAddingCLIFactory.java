package drivers.resourceadding;

import drivers.common.cli.ICLIHelper;

import common.map.IMutableMapNG;

import drivers.common.SPOptions;
import drivers.common.ModelDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.IDriverModel;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to let the user enter a player's resources and equipment.
 */
@AutoService(DriverFactory.class)
public class ResourceAddingCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "add-resource",
		ParamCount.AtLeastOne, "Add resources to maps", "Add resources for players to maps.",
		true, false, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof ResourceManagementDriverModel) {
			return new ResourceAddingCLI(cli, options, (ResourceManagementDriverModel) model);
		} else {
			return createDriver(cli, options, new ResourceManagementDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new ResourceManagementDriverModel(map);
	}
}
