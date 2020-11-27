import strategicprimer.drivers.common {
    DriverUsage,
    IDriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

import lovelace.util.common {
    PathWrapper
}

"A driver for an app to copy selected contents from one map to another."
service(`interface DriverFactory`)
shared class MapTradeFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "trade-maps", ParamCount.two,
        "Trade maps", "Copy contents from one map to another.", true, false, "source.xml",
        "destination.xml");
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is MapTradeModel model) {
            return MapTradeCLI(cli, model);
        } else {
            return createDriver(cli, options, MapTradeModel.copyConstructor(model));
        }
    }
    shared actual MapTradeModel createModel(IMutableMapNG map, PathWrapper? path) =>
            MapTradeModel(map, path);
}
