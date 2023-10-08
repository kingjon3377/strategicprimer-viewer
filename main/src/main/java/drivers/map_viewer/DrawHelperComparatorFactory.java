package drivers.map_viewer;

import com.google.auto.service.AutoService;

import drivers.common.SPOptions;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

/**
 * A factory for a driver to compare the performance of TileDrawHelpers.
 */
@AutoService(DriverFactory.class)
public class DrawHelperComparatorFactory implements UtilityDriverFactory {
    public static final IDriverUsage USAGE = new DriverUsage(true, "drawing-performance", ParamCount.AtLeastOne,
            "Test drawing performance.",
            "Test the performance of the TileDrawHelper classes---which do the heavy lifting of rendering the map in the viewer---using a variety of automated tests.",
            true, false, "--report=out.csv");

    @Override
    public IDriverUsage getUsage() {
        return USAGE;
    }

    @Override
    public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
        return new DrawHelperComparator(cli, options);
    }
}
