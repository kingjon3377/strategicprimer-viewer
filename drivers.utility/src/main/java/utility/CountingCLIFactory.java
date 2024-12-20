package utility;

import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.ModelDriver;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;

import drivers.common.SimpleDriverModel;
import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;
import legacy.map.IMutableLegacyMap;

import java.util.EnumSet;

/**
 * A factory for an app to report statistics on the contents of the map.
 */
@AutoService(DriverFactory.class)
public final class CountingCLIFactory implements ModelDriverFactory<IDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "count",
			ParamCount.One, "Calculate statistics of map contents", "Print statistical report of map contents.",
			EnumSet.noneOf(IDriverUsage.DriverMode.class));

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		return new CountingCLI(cli, model);
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map)  {
		return new SimpleDriverModel(map);
	}

	@Override
	public IDriverModel createModel(IDriverModel model) {
		return new SimpleDriverModel(model);
	}
}
