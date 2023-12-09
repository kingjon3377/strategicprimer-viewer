package drivers;

import java.io.File;

import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.ModelDriverFactory;
import drivers.common.DriverFactory;
import drivers.common.ModelDriver;
import drivers.common.SimpleMultiMapModel;

import drivers.common.cli.ICLIHelper;

import legacy.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to produce a report of the contents of a map.
 */
@AutoService(DriverFactory.class)
public class ReportCLIFactory implements ModelDriverFactory {
    private static final IDriverUsage USAGE = new DriverUsage(false, "create-report",
            ParamCount.AtLeastOne, "Report Generator", "Produce HTML report of the contents of a map",
            true, false,
            ('\\' == File.separatorChar) ? "--out=C:\\path\\to\\output.html"
                    : "--out=/path/to/output.html",
            "--player=NN", "--current-turn=NN", "--serve[=8080]");

    @Override
    public IDriverUsage getUsage() {
        return USAGE;
    }

    @Override
    public ModelDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (options.hasOption("--serve")) {
            return new ReportServingCLI(options, model, cli);
        } else {
            return new ReportCLI(options, model, cli);
        }
    }

    @Override
    public IDriverModel createModel(final IMutableMapNG map) {
        return new SimpleMultiMapModel(map);
    }
}
