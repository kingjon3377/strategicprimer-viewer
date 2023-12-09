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
    public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (model instanceof final ResourceManagementDriverModel rmdm) {
            return new ResourceAddingCLI(cli, options, rmdm);
        } else {
            return createDriver(cli, options, new ResourceManagementDriverModel(model));
        }
    }

    @Override
    public IDriverModel createModel(final IMutableLegacyMap map) {
        return new ResourceManagementDriverModel(map);
    }
}
