package drivers.exploration;

import java.util.logging.Logger;

import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for the CLI exploration app.
 */
@AutoService(DriverFactory.class)
public class ExplorationCLIFactory implements ModelDriverFactory {
	private static final Logger LOGGER = Logger.getLogger(ExplorationCLIFactory.class.getName());
	private static final IDriverUsage USAGE = new DriverUsage(false, "explore", ParamCount.AtLeastOne,
		"Run exploration.",
		"Move a unit around the map, updating the player's map with what it sees.",
		true, false, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof IExplorationModel) {
			return new ExplorationCLI(cli, (IExplorationModel) model);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new ExplorationModel(map);
	}
}
