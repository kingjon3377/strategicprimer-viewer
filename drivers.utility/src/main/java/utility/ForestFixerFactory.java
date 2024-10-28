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

/**
 * A factory for a driver to fix ID mismatches between forests and Ground in the main and player maps.
 */
@AutoService(DriverFactory.class)
public final class ForestFixerFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "fix-forests",
			ParamCount.AtLeastTwo, "Fix forest IDs",
			"Make sure that forest IDs in submaps match the main map", false, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final UtilityDriverModel udm) {
			return new ForestFixerDriver(cli, udm);
		} else {
			return createDriver(cli, options, new UtilityDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new UtilityDriverModel(map);
	}
}
