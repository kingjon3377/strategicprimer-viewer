import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    DriverFactory,
    UtilityDriverFactory
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A factory for a driver to compare the performance and results of the two map reading
 implementations."
service(`interface DriverFactory`)
shared class ReaderComparatorFactory() satisfies UtilityDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, "compare-readers",
        ParamCount.atLeastOne, "Test map readers",
        "Test map-reading implementations by comparing their results on the same file.",
        true, false);

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            ReaderComparator(cli);
}
