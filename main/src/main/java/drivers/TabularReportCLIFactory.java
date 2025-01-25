package drivers;

import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;
import drivers.common.SimpleMultiMapModel;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;


import com.google.auto.service.AutoService;

import java.util.EnumSet;

/**
 * A factory for a driver to produce tabular (CSV) reports of the contents of a player's map.
 */
@AutoService(DriverFactory.class)
public final class TabularReportCLIFactory implements ModelDriverFactory<IDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "tabular-report",
			ParamCount.AtLeastOne, "Tabular Report Generator",
			"Produce CSV reports of the contents of a map.", EnumSet.of(IDriverUsage.DriverMode.CommandLine),
			"--serve[=8080]");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (options.hasOption("--serve")) {
			return new TabularReportServingCLI(cli, options, model);
		} else {
			return new TabularReportCLI(cli, options, model);
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new SimpleMultiMapModel(map);
	}

	@Override
	public IDriverModel createModel(final IDriverModel model) {
		return new SimpleMultiMapModel(model);
	}
}
