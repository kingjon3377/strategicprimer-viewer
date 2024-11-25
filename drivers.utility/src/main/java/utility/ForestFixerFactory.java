package utility;

import drivers.common.DriverFactory;
import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.ModelDriver;
import drivers.common.ModelDriverFactory;
import drivers.common.ParamCount;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to fix ID mismatches between forests and Ground in the main and player maps.
 */
@AutoService(DriverFactory.class)
public final class ForestFixerFactory implements ModelDriverFactory<UtilityDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "fix-forests",
			ParamCount.AtLeastTwo, "Fix forest IDs",
			"Make sure that forest IDs in submaps match the main map", EnumSet.noneOf(IDriverUsage.DriverMode.class));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final UtilityDriverModel model) {
		return new ForestFixerDriver(cli, model);
	}

	@Override
	public UtilityDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}

	@Override
	public UtilityDriverModel createModel(IDriverModel model) {
		return new UtilityDriverModel(model);
	}
}
