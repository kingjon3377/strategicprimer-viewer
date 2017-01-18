package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import controller.map.misc.MenuBroker;
import controller.map.misc.WindowCloser;
import javax.swing.SwingUtilities;
import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.misc.IDriverModel;
import view.exploration.ExplorationFrame;
import view.util.AboutDialog;
import view.util.DriverQuit;

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
										   "map with what it sees.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
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
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(explorationModel), "load", "save", "save as",
				"new", "load secondary", "save all", "open in map viewer",
				"open secondary map in map viewer");
		menuHandler.register(evt -> DriverQuit.quit(0), "quit");
		SwingUtilities.invokeLater(
				() -> {
					final ExplorationFrame frame =
							new ExplorationFrame(explorationModel, menuHandler);
					menuHandler.register(new WindowCloser(frame), "close");
					menuHandler.register(evt -> new AboutDialog(frame, frame.getWindowName())
														.setVisible(true), "about");
					frame.setVisible(true);
				});
	}

	/**
	 * The usage object.
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationGUI";
	}
}
