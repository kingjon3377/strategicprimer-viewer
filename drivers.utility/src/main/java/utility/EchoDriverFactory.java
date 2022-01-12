package utility;

import java.io.IOException;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;
import common.map.IMutableMapNG;
import common.map.fixtures.Ground;
import common.map.fixtures.terrain.Forest;
import impl.xmlio.MapIOHelper;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;
import drivers.common.cli.ICLIHelper;
import lovelace.util.MalformedXMLException;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver that reads in maps and then writes them out again, to
 * test the map-reading logic and to correct deprecated syntax.
 */
@AutoService(DriverFactory.class)
public class EchoDriverFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(false, "echo", ParamCount.Two,
		"Read, then write a map.", "Read and write a map, correcting deprecated syntax.",
		true, false, "input.xml", "output.xml", "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(ICLIHelper cli, SPOptions options) {
		return new EchoDriver(options);
	}
}

