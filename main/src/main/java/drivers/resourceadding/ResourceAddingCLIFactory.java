package drivers.resourceadding;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import drivers.common.SPOptions;
import drivers.common.ModelDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to let the user enter a player's resources and equipment.
 */
@AutoService(DriverFactory.class)
public final class ResourceAddingCLIFactory implements ModelDriverFactory<ResourceManagementDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "add-resource",
			ParamCount.AtLeastOne, "Add resources to maps", "Add resources for players to maps.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options,
	                                final ResourceManagementDriverModel model) {
		return new ResourceAddingCLI(cli, options, model);
	}

	@Override
	public ResourceManagementDriverModel createModel(final IMutableLegacyMap map) {
		return new ResourceManagementDriverModel(map);
	}
}
