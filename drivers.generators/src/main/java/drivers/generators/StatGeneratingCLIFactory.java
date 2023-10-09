package drivers.generators;

import common.map.IMutableMapNG;

import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to generate new workers.
 */
@AutoService(DriverFactory.class)
public class StatGeneratingCLIFactory implements ModelDriverFactory {
    private static final IDriverUsage USAGE = new DriverUsage(false, "generate-stats",
            ParamCount.AtLeastOne, "Generate new workers.",
            "Generate new workers with random stats and experience.", true, false, "--current-turn=NN");

    @Override
    public IDriverUsage getUsage() {
        return USAGE;
    }

    @Override
    public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (model instanceof final PopulationGeneratingModel pgm) {
            return new StatGeneratingCLI(cli, pgm);
        } else {
            return createDriver(cli, options, new PopulationGeneratingModel(model));
        }
    }

    @Override
    public IDriverModel createModel(final IMutableMapNG map) {
        return new PopulationGeneratingModel(map);
    }
}
