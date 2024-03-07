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

/**
 * A factory for a driver to produce tabular (CSV) reports of the contents of a player's map.
 */
@AutoService(DriverFactory.class)
public class TabularReportCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "tabular-report",
			ParamCount.AtLeastOne, "Tabular Report Generator",
			"Produce CSV reports of the contents of a map.", true, false, "--serve[=8080]");

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
}
