package drivers.exploration.old;

import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;

import drivers.common.cli.ICLIHelper;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to help debug "exploration tables", which were the
 * second "exploration results" framework I implemented.
 */
@AutoService(DriverFactory.class)
public class TableDebuggerFactory implements UtilityDriverFactory {
	public static final IDriverUsage USAGE = new DriverUsage(false, "table-debug",
		ParamCount.None, "Debug old-model encounter tables",
		"See whether old-model encounter tables refer to a nonexistent table", false, false);

	public TableDebuggerFactory() {
	}

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	@Override
	public UtilityDriver createDriver(final ICLIHelper cli, final SPOptions options) {
		return new TableDebugger(cli::println);
	}
}
