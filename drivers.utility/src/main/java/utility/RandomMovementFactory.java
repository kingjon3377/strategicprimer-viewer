package utility;

import drivers.common.SPOptions;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
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
 * A factory for an app to move independent units around at random.
 *
 * TODO: We'd like a GUI for this, perhaps adding customization or limiting the area or something
 */
@AutoService(DriverFactory.class)
public final class RandomMovementFactory implements ModelDriverFactory<IExplorationModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "random-move",
			ParamCount.One, "Move independent units at random", "Move independent units randomly around the map.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IExplorationModel model) {
		return new RandomMovementCLI(options, model);
	}

	@Override
	public IExplorationModel createModel(final IMutableLegacyMap map) {
		return new ExplorationModel(map);
	}
}
