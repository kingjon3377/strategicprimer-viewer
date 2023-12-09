package drivers.turnrunning;

import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for the turn-running CLI app.
 */
@AutoService(DriverFactory.class)
public class TurnRunningCLIFactory implements ModelDriverFactory {
    private static final IDriverUsage USAGE = new DriverUsage(false, "run-turn", ParamCount.AtLeastTwo,
            "Run a turn's orders and enter results", "Run a player's orders for a turn and enter results.",
            true, false, "--current-turn=NN");

    @Override
    public IDriverUsage getUsage() {
        return USAGE;
    }

    @Override
    public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (model instanceof final ITurnRunningModel trm) {
            return new TurnRunningCLI(cli, trm);
        } else {
            return createDriver(cli, options, new TurnRunningModel(model));
        }
    }

    @Override
    public IDriverModel createModel(final IMutableMapNG map) {
        return new TurnRunningModel(map);
    }
}
