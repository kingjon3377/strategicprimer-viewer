package utility;

import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for an app to report statistics on the contents of the map.
 */
@AutoService(DriverFactory.class)
public final class CountingCLIFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "count", ParamCount.One,
			"Calculate statistics of map contents", "Print statistical report of map contents.",
			false, false);

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		return new CountingCLI(cli, model);
	}
}
