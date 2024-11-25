package drivers.exploration;

import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for the CLI exploration app.
 */
@AutoService(DriverFactory.class)
public final class ExplorationCLIFactory implements ModelDriverFactory<IExplorationModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "explore",
			ParamCount.AtLeastOne, "Run exploration.",
			"Move a unit around the map, updating the player's map with what it sees.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IExplorationModel model) {
		return new ExplorationCLI(cli, model);
	}

	@Override
	public IExplorationModel createModel(final IMutableLegacyMap map) {
		return new ExplorationModel(map);
	}

	@Override
	public IExplorationModel createModel(IDriverModel model) {
		return new ExplorationModel(model);
	}
}
