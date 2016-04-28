package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.IOHandler;
import javax.swing.SwingUtilities;
import model.exploration.ExplorationModel;
import model.misc.IDriverModel;
import view.exploration.ExplorationFrame;

/**
 * A class to start the exploration GUI.
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
public final class ExplorationGUI implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-x", "--explore", ParamCount.AtLeastOne,
								"Run exploration.",
								"Move a unit around the map, updating the player's " +
										"map with what it sees.",
								ExplorationGUI.class);

	/**
	 * Run the driver.
	 *
	 * @param model the driver model
	 */
	@Override
	public void startDriver(final IDriverModel model) {
		final ExplorationModel emodel;
		if (model instanceof ExplorationModel) {
			emodel = (ExplorationModel) model;
		} else {
			emodel = new ExplorationModel(model);
		}
		SwingUtilities.invokeLater(
				() -> new ExplorationFrame(emodel, new IOHandler(emodel)).setVisible(true));
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
		return "ExplorationGUI";
	}
}
