package utility;

import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;
import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver that reads in maps and then writes them out again, to
 * test the map-reading logic and to correct deprecated syntax.
 */
@AutoService(DriverFactory.class)
public final class EchoDriverFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(false, "echo", ParamCount.Two,
			"Read, then write a map.", "Read and write a map, correcting deprecated syntax.",
			true, false, "input.xml", "output.xml", "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new EchoDriver(options);
	}
}

