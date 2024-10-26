package utility;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

/**
 * An app to produce a difference between two maps, to aid understanding what
 * an explorer has found. This modifies non-main maps in place; only run on
 * copies or under version control!
 */
public final class SubtractCLI implements CLIDriver {
	public SubtractCLI(final UtilityDriverModel model) {
		this.model = model;
	}

	private final UtilityDriverModel model;

	@Override
	public UtilityDriverModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return EmptyOptions.EMPTY_OPTIONS;
	}

	@Override
	public void startDriver() {
		model.getMap().getLocations().forEach(model::subtractAtPoint);
	}
}
