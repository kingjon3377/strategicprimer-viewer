package utility;

import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.DriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.ModelDriver;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableLegacyMap;

import com.google.auto.service.AutoService;

/**
 * A driver for an app to copy selected contents from one map to another.
 */
@AutoService(DriverFactory.class)
public final class MapTradeFactory implements ModelDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.CommandLine, "trade-maps",
			ParamCount.Two, "Trade maps", "Copy contents from one map to another.", true, false, "source.xml",
			"destination.xml");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		// TODO: If any options, print a warning (here and in other empty-options drivers
		if (model instanceof final MapTradeModel mtm) {
			return new MapTradeCLI(cli, mtm);
		} else {
			return createDriver(cli, options, new MapTradeModel(model));
		}
	}

	@Override
	public MapTradeModel createModel(final IMutableLegacyMap map) {
		return new MapTradeModel(map);
	}
}
