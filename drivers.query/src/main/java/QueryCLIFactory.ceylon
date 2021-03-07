
import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.common {
    SPOptions,
    ParamCount,
    IDriverUsage,
    DriverUsage,
    IDriverModel,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver,
    SimpleDriverModel
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"""A factory for the driver to "query" the driver model about various things."""
service(`interface DriverFactory`)
shared class QueryCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "query",
        ParamCount.one, "Answer questions about a map.",
        "Answer questions about a map, such as counting workers or calculating distances.", true, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) => QueryCLI(cli, model);

    shared actual IDriverModel createModel(IMutableMapNG map) => SimpleDriverModel(map);

}
