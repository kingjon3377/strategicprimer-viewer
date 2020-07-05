import strategicprimer.drivers.common {
    DriverFactory,
    ModelDriverFactory,
    ParamCount,
    DriverUsage,
    IDriverUsage,
    ModelDriver,
    SPOptions,
    IDriverModel,
    IMultiMapModel,
    SimpleMultiMapModel
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
"An app to produce a difference between two maps, to aid understanding what an explorer has
 found. This modifies non-main maps in place; only run on copies or under version control!"
service(`interface DriverFactory`)
shared class SubtractFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "subtract", ParamCount.atLeastTwo,
        "Subtract one map from another", "Remove everything known in a base map from submaps for easier comparison",
        false, false, "baseMap.xml", "operand.xml");
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        if (is IMultiMapModel model) {
            return SubtractCLI(model);
        } else {
            return createDriver(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) => SimpleMultiMapModel(map, path);
}
