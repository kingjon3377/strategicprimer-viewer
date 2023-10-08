package mining;

import drivers.common.UtilityDriver;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.SPOptions;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to create a spreadsheet model of a mine. Its
 * parameters are the name of the file to write the CSV to and the value at the
 * top center (as an index into the {@link LodeStatus} values array, or the String
 * representation of the desired value).
 */
@AutoService(DriverFactory.class)
public final class MiningCLIFactory implements UtilityDriverFactory {
    public static final IDriverUsage USAGE = new DriverUsage(false, "mining", ParamCount.Two,
            "Create a model of a mine", "Create a CSV spreadsheet representing a mine's area",
            true, false, "output.csv", "status", "--seed=NN", "--banded");

    @Override
    public IDriverUsage getUsage() {
        return USAGE;
    }

    @Override
    public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
        return new MiningCLI(cli, options);
    }
}
