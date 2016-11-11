package controller.map.drivers;

import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import java.io.IOException;
import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.misc.IDriverModel;
import view.exploration.ExplorationCLI;

/**
 * A CLI to help running exploration.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationCLIDriver implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-x", "--explore", ParamCount.AtLeastOne,
								"Run exploration.",
								"Move a unit around the map, updating the player's " +
										"map with what it sees.",
								ExplorationCLIDriver.class);

	/**
	 * Run the driver.
	 *
	 * @param options
	 * @param model the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final SPOptions options, final IDriverModel model)
			throws DriverFailedException {
		final IExplorationModel explorationModel;
		if (model instanceof IExplorationModel) {
			explorationModel = (IExplorationModel) model;
		} else {
			explorationModel = new ExplorationModel(model);
		}
		try (final ICLIHelper cliHelper = new CLIHelper()) {
			final ExplorationCLI cli = new ExplorationCLI(explorationModel, cliHelper);
			final Player player = cli.choosePlayer();
			if (player == null) {
				return;
			}
			final IUnit unit = cli.chooseUnit(player);
			if (unit == null) {
				return;
			} else {
				explorationModel.selectUnit(unit);
				cli.moveUntilDone();
			}
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationCLIDriver";
	}
}
