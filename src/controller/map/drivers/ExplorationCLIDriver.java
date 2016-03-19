package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.ICLIHelper;
import java.io.IOException;
import model.exploration.ExplorationModel;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.misc.IDriverModel;
import view.exploration.ExplorationCLI;
import view.util.DriverQuit;

import static view.util.SystemOut.SYS_OUT;

/**
 * A CLI to help running exploration.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationCLIDriver implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-x", "--explore", ParamCount.Many, "Run exploration.",
								   "Move a unit around the map, updating the player's " +
										   "map with what it sees.",
								   ExplorationCLIDriver.class);

	/**
	 * Run the driver.
	 *
	 * @param model the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		final ExplorationModel emodel;
		if (model instanceof ExplorationModel) {
			emodel = (ExplorationModel) model;
		} else {
			emodel = new ExplorationModel(model);
		}
		try (final ICLIHelper clih = new CLIHelper()) {
			final ExplorationCLI cli = new ExplorationCLI(emodel, clih);
			final Player player = cli.choosePlayer();
			if (player == null) {
				return;
			}
			final IUnit unit = cli.chooseUnit(player);
			if (unit == null) {
				return;
			} else {
				emodel.selectUnit(unit);
				cli.moveUntilDone();
			}
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args the command-line arguments
	 * @throws DriverFailedException on error.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.print("Usage: ");
			SYS_OUT.print(getClass().getSimpleName());
			SYS_OUT.println(" master-map [player-map ...]");
			DriverQuit.quit(1);
		}
		SimpleCLIDriver.super.startDriver(args);
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
