package utility;

import drivers.common.CLIDriver;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

/**
 * A driver to fix ID mismatches between forests and Ground in the main and player maps.
 */
public class ForestFixerDriver implements CLIDriver {
	public ForestFixerDriver(ICLIHelper cli, SPOptions options, UtilityDriverModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;
	private final SPOptions options; // TODO: Use EmptyOptions instead of taking a parameter
	private final UtilityDriverModel model;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	@Override
	public UtilityDriverModel getModel() {
		return model;
	}

	@Override
	public void startDriver() {
		model.fixForestsAndGround(cli::println);
	}
}
