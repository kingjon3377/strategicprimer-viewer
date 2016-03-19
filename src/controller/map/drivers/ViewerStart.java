package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import java.io.File;
import java.util.stream.StreamSupport;
import javax.swing.*;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import view.map.main.ViewerFrame;

/**
 * A class to start the viewer, to reduce circular dependencies between packages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class ViewerStart implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(true, "-m", "--map", ParamCount.One, "Map viewer",
								   "Look at the map visually. This is probably the app " +
										   "you want.",
								   ViewerStart.class);

	/**
	 * Run the driver.
	 *
	 * @param model the driver model
	 */
	@Override
	public void startDriver(final IDriverModel model) {
		final IViewerModel vmodel;
		if (model instanceof IViewerModel) {
			vmodel = (IViewerModel) model;
		} else if (model instanceof IMultiMapModel) {
			StreamSupport
					.stream(((IMultiMapModel) model).getAllMaps().spliterator(), false)
					.map(ViewerModel::new).forEach(this::startDriver);
			return;
		} else {
			vmodel = new ViewerModel(model);
		}
		SwingUtilities.invokeLater(
				() -> new ViewerFrame(vmodel, new IOHandler(vmodel)).setVisible(true));
	}

	/**
	 * Run the driver.
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			try {
				startDriver(new FileChooser(new File("")).getFile().getPath());
			} catch (final ChoiceInterruptedException except) {
				throw new DriverFailedException("File choice was interrupted or user " +
														"didn't choose",
													   except);
			}
		} else {
			// We allow the default implementation to get a MultiMapModel so the
			// overload that takes a map-model can start one window for each map,
			// without having to make multiple calls to the reader.
			SimpleDriver.super.startDriver(args);
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
		return "ViewerStart";
	}
}
