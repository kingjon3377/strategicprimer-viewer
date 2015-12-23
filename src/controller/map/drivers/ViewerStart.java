package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;
import java.io.File;
import javax.swing.*;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import util.Pair;
import util.Warning;
import util.Warning.Action;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.FilteredFileChooser;

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
public final class ViewerStart implements ISPDriver {
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
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		final IViewerModel model;
		if (dmodel instanceof IViewerModel) {
			model = (IViewerModel) dmodel;
		} else if (dmodel instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, File> pair : ((IMultiMapModel) dmodel)
					                                            .getAllMaps()) {
				startDriver(new ViewerModel(pair.first(), pair.second()));
			}
			return;
		} else {
			model = new ViewerModel(dmodel);
		}
		SwingUtilities
				.invokeLater(new WindowThread(new ViewerFrame(model, new IOHandler(model,
						                                                                  new FilteredFileChooser(".",
								                                                                                         new MapFileFilter())))));
	}

	/**
	 * Run the driver. TODO: Somehow unify similar code between this and other similar
	 * drivers.
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 */
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
			// We get a MultiMapModel so the overload that takes a map-model can
			// start one window for each map, without having to make multiple
			// calls to the reader.
			startDriver(new MapReaderAdapter().readMultiMapModel(new Warning(Action
					                                                                 .Warn),
					new File(args[0]), MapReaderAdapter.namesToFiles(true, args)));
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
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ViewerStart";
	}
}
