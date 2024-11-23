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

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for the turn-running CLI app.
 */
@AutoService(DriverFactory.class)
public final class TurnRunningCLIFactory implements ModelDriverFactory<ITurnRunningModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "run-turn",
			ParamCount.AtLeastTwo, "Run a turn's orders and enter results",
			"Run a player's orders for a turn and enter results.", EnumSet.of(IDriverUsage.DriverMode.CommandLine),
			"--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final ITurnRunningModel model) {
		return new TurnRunningCLI(cli, model);
	}

	@Override
	public ITurnRunningModel createModel(final IMutableLegacyMap map) {
		return new TurnRunningModel(map);
	}
}
