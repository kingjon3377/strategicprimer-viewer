package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import util.Warning;
import util.Warning.Action;
import view.worker.WorkerMgmtFrame;

/**
 * A class to start the user worker management GUI.
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
public final class WorkerStart implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-w", "--worker", ParamCount.Many,
					               "Manage a player's workers in units",
					               "Organize the members of a player's units.",
					               WorkerStart.class);

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}

	/**
	 * Run the driver. This form is, at the moment, primarily for use in test code, but
	 * that may change.
	 *
	 * @param dmodel the driver-model that should be used by the app
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) {
		final IWorkerModel model;
		if (dmodel instanceof IWorkerModel) {
			model = (IWorkerModel) dmodel;
		} else {
			model = new WorkerModel(dmodel);
		}
		SwingUtilities.invokeLater(
				() -> new WorkerMgmtFrame(model, new IOHandler(model)).setVisible(true));
	}

	/**
	 * Run the driver.
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final File file; // NOPMD
		try {
			if (args.length == 0) {
				file = new FileChooser(new File("")).getFile();
			} else {
				file = new FileChooser(new File(args[0])).getFile();
			}
		} catch (final ChoiceInterruptedException except) {
			throw new DriverFailedException("File choice was interrupted or user didn't choose",
					                               except);
		}
		final IWorkerModel model = new WorkerModel(
				                                          new MapReaderAdapter()
						                                          .readMultiMapModel(
								                                          new Warning(Action.Warn),
								                                          file,
								                                          MapReaderAdapter
										                                          .namesToFiles(
												                                          true,
												                                          args)));
		SwingUtilities.invokeLater(
				() -> new WorkerMgmtFrame(model, new IOHandler(model)).setVisible(true));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "WorkerStart";
	}
}
