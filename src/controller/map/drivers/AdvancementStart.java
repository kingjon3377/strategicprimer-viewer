package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import controller.map.misc.MenuBroker;
import controller.map.misc.PlayerChangeMenuListener;
import controller.map.misc.WindowCloser;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import view.util.AboutDialog;
import view.util.DriverQuit;
import view.worker.AdvancementFrame;

/**
 * A class to start the worker management GUI.
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
public final class AdvancementStart implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-a", "--adv", ParamCount.AtLeastOne,
								   "View a player's workers and manage their " +
										   "advancement",
								   "View a player's units, the workers in those units, " +
										   "each worker's Jobs, and his or her level in" +
										   " each Skill in each Job.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Run the driver
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IWorkerModel workerModel;
		if (model instanceof IWorkerModel) {
			workerModel = (IWorkerModel) model;
		} else {
			workerModel = new WorkerModel(model);
		}
		final IOHandler ioh = new IOHandler(workerModel);
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(ioh, "load", "save", "save as", "new", "load secondary",
				"save all", "open in map viewer", "open secondary map in map viewer");
		final PlayerChangeMenuListener pcml = new PlayerChangeMenuListener(workerModel);
		menuHandler.register(pcml, "change current player");
		menuHandler.register(evt -> DriverQuit.quit(0), "quit");
		SwingUtilities.invokeLater(
				() -> {
					final AdvancementFrame frame =
							new AdvancementFrame(workerModel, menuHandler);
					pcml.addPlayerChangeListener(frame);
					menuHandler.register(
							evt -> frame.playerChanged(model.getMap().getCurrentPlayer(),
									model.getMap().getCurrentPlayer()), "reload tree");
					menuHandler.register(new WindowCloser(frame), "close");
					menuHandler.register(evt -> new AboutDialog(frame, frame.getTitle())
														.setVisible(true), "about");
					frame.setVisible(true);
				});
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AdvancementStart";
	}
}
