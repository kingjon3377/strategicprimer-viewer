package controller.map.drivers;

import controller.map.misc.FindHandler;
import controller.map.misc.ICLIHelper;
import controller.map.misc.IOHandler;
import controller.map.misc.MenuBroker;
import controller.map.misc.WindowCloser;
import javax.swing.SwingUtilities;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import view.map.main.SelectTileDialog;
import view.map.main.ViewerFrame;
import view.map.main.ZoomListener;
import view.util.AboutDialog;
import view.util.DriverQuit;

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
			((IMultiMapModel) model).streamAllMaps().map(ViewerModel::new)
					.forEach(mapModel -> startDriver(cli, options.copy(), mapModel));
			return;
		} else {
			viewerModel = new ViewerModel(model);
		}
		final MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(viewerModel), "load", "save", "save as",
				"new", "load secondary", "save all", "open in map viewer",
				"open secondary map in map viewer");
		menuHandler.register(evt -> DriverQuit.quit(0), "quit");
		menuHandler.register(evt -> viewerModel.zoomIn(), "zoom in");
		menuHandler.register(evt -> viewerModel.zoomOut(), "zoom out");
		menuHandler.register(new ZoomListener(viewerModel), "center");
		SwingUtilities.invokeLater(
				() -> {
					final ViewerFrame frame = new ViewerFrame(viewerModel, menuHandler);
					menuHandler.register(new WindowCloser(frame), "close");
					menuHandler.register(evt -> new SelectTileDialog(frame, viewerModel)
														.setVisible(true), "go to tile");
					menuHandler.register(new FindHandler(frame, viewerModel),
							"find a fixture", "find next");
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
		return "ViewerStart";
	}
}
