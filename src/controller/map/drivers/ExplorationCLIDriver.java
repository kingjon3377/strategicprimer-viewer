package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.misc.CLIHelper;
import controller.map.misc.MapReaderAdapter;
import model.exploration.ExplorationModel;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.misc.IDriverModel;
import util.Warning;
import view.exploration.ExplorationCLI;

/**
 * A CLI to help running exploration.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationCLIDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-x",
			"--explore", ParamCount.Many, "Run exploration.",
			"Move a unit around the map, "
					+ "updating the player's map with what it sees.",
					ExplorationCLIDriver.class);

	/**
	 * Run the driver.
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final ExplorationModel model;
		if (dmodel instanceof ExplorationModel) {
			model = (ExplorationModel) dmodel;
		} else {
			model = new ExplorationModel(dmodel);
		}
		final ExplorationCLI cli = new ExplorationCLI(model, new CLIHelper());
		try {
			final Player player = cli.choosePlayer();
			if (player == null) {
				return;
			}
			final IUnit unit = cli.chooseUnit(player);
			if (unit == null) {
				return;
			} else {
				model.selectUnit(unit);
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
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.print("Usage: ");
			SYS_OUT.print(getClass().getSimpleName());
			SYS_OUT.println(" master-map [player-map ...]");
			System.exit(1);
		}
		final MapReaderAdapter reader = new MapReaderAdapter();
		final IDriverModel model = new ExplorationModel(
				reader.readMultiMapModel(Warning.INSTANCE, new File(args[0]),
						MapReaderAdapter.namesToFiles(true, args)));
		startDriver(model);
		reader.writeModel(model);
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return usage().getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationCLIDriver";
	}
}
