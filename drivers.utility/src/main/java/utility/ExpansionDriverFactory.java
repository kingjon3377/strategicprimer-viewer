package utility;

import legacy.map.IMutableLegacyMap;

import drivers.common.IDriverModel;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to update a player's map to include a certain minimum
 * distance around allied villages.
 */
@AutoService(DriverFactory.class)
public final class ExpansionDriverFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "expand",
			ParamCount.AtLeastTwo, "Expand a player's map.",
			"Ensure a player's map covers all terrain allied villages can see.", true,
			false, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final UtilityDriverModel udm) {
			return new ExpansionDriver(options, udm);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}
}
