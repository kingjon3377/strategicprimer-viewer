package utility;

import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for an app to move independent units around at random.
 *
 * TODO: We'd like a GUI for this, perhaps adding customization or limiting the area or something
 */
@AutoService(DriverFactory.class)
public class RandomMovementFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "random-move", ParamCount.One,
		"Move independent units at random", "Move independent units randomly around the map.",
		true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IExplorationModel) {
			return new RandomMovementCLI(cli, options, (IExplorationModel) model);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new ExplorationModel(map);
	}
}
