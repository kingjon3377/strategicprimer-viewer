package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import java.util.stream.StreamSupport;
import javax.swing.SwingUtilities;
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
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
										   "you want.");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * Run the driver. If the model is a multi-map model, we open one window per map.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IViewerModel viewerModel;
		if (model instanceof IViewerModel) {
			viewerModel = (IViewerModel) model;
		} else if (model instanceof IMultiMapModel) {
			StreamSupport
					.stream(((IMultiMapModel) model).getAllMaps().spliterator(), false)
					.map(ViewerModel::new)
					.forEach(mapModel -> startDriver(cli, options.copy(), mapModel));
			return;
		} else {
			viewerModel = new ViewerModel(model);
		}
		SwingUtilities.invokeLater(
				() -> new ViewerFrame(viewerModel, new IOHandler(viewerModel))
							  .setVisible(true));
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
		return "ViewerStart";
	}
}
