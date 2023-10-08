package utility;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import drivers.common.cli.ICLIHelper;

/**
 * A driver to fix ID mismatches between forests and Ground in the main and player maps.
 */
public class ForestFixerDriver implements CLIDriver {
    public ForestFixerDriver(final ICLIHelper cli, final UtilityDriverModel model) {
        this.cli = cli;
        this.model = model;
    }

    private final ICLIHelper cli;
    private final UtilityDriverModel model;

    @Override
    public SPOptions getOptions() {
        return EmptyOptions.EMPTY_OPTIONS;
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
