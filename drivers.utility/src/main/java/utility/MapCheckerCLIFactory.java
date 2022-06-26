package utility;

import drivers.common.UtilityDriver;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.UtilityDriverFactory;
import drivers.common.DriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to check every map file in a list for errors.
 */
@AutoService(DriverFactory.class)
public class MapCheckerCLIFactory implements UtilityDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "check",
		ParamCount.AtLeastOne, "Check map for errors",
		"Check a map file for errors, deprecated syntax, etc.", true, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new MapCheckerCLI(cli::println, cli::println);
	}
}
