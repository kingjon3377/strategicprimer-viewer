package utility;

import drivers.common.IDriverModel;
import legacy.map.IMutableLegacyMap;

import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to update a player's map to include a certain minimum
 * distance around allied villages.
 */
@AutoService(DriverFactory.class)
public final class ExpansionDriverFactory implements ModelDriverFactory<UtilityDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "expand",
			ParamCount.AtLeastTwo, "Expand a player's map.",
			"Ensure a player's map covers all terrain allied villages can see.",
			EnumSet.of(IDriverUsage.DriverMode.CommandLine), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final UtilityDriverModel model) {
		return new ExpansionDriver(options, model);
	}

	@Override
	public UtilityDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}

	@Override
	public UtilityDriverModel createModel(final IDriverModel model) {
		return new UtilityDriverModel(model);
	}
}
