package drivers.exploration;

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

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

/**
 * A factory for the CLI exploration app.
 */
@AutoService(DriverFactory.class)
public final class ExplorationCLIFactory implements ModelDriverFactory {
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
		if (model instanceof final IExplorationModel em) {
			return new ExplorationCLI(cli, em);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new ExplorationModel(map);
	}
}
