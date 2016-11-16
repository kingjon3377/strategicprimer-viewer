package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import javax.swing.SwingUtilities;
import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.misc.IDriverModel;
import view.exploration.ExplorationFrame;

/**
 * A class to start the exploration GUI.
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
public final class ExplorationGUI implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-x", "--explore", ParamCount.AtLeastOne,
								"Run exploration.",
								"Move a unit around the map, updating the player's " +
										"map with what it sees."
			);
	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Run the driver.
	 *
	 * @param cli
	 * @param options
	 * @param model the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IExplorationModel explorationModel;
		if (model instanceof IExplorationModel) {
			explorationModel = (IExplorationModel) model;
		} else {
			explorationModel = new ExplorationModel(model);
		}
		SwingUtilities.invokeLater(() -> new ExplorationFrame(explorationModel,
																	 new IOHandler
																			 (explorationModel))
												 .setVisible(true));
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
