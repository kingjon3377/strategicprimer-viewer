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

import common.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A driver for an app to copy selected contents from one map to another.
 */
@AutoService(DriverFactory.class)
public class MapTradeFactory implements ModelDriverFactory {
	private static final IDriverUsage USAGE = new DriverUsage(false, "trade-maps", ParamCount.Two,
		"Trade maps", "Copy contents from one map to another.", true, false, "source.xml",
		"destination.xml");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof MapTradeModel) { // TODO: If any options, print a warning (here and in other empty-options drivers
			return new MapTradeCLI(cli, (MapTradeModel) model);
		} else {
			return createDriver(cli, options, new MapTradeModel(model));
		}
	}

	@Override
	public MapTradeModel createModel(IMutableMapNG map) {
		return new MapTradeModel(map);
	}
}
