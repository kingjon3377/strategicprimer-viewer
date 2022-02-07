package drivers.map_viewer;

import drivers.common.DriverFailedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.nio.file.Path;

import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.cli.ICLIHelper;
import common.map.IMutableMapNG;
import drivers.gui.common.SPFileChooser;

import lovelace.util.FileChooser;

import java.util.Collections;

import com.google.auto.service.AutoService;

/**
 * A factory for a driver to start the map viewer.
 */
@AutoService(DriverFactory.class)
public class ViewerGUIFactory implements GUIDriverFactory {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerGUIFactory.class.getName());

	private static final IDriverUsage USAGE = new DriverUsage(true, "view-map", ParamCount.One,
		"Map viewer", "Look at the map visually. This is probably the app you want.",
		false, true, "--current-turn=NN", "--background=image.png",
		"--starting-row=NN --starting-column=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	public Iterable<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		return ViewerGUI.createDriver(cli, options, model);
	}

	@Override
	public IViewerModel createModel(final IMutableMapNG map) {
		Path path = map.getFilename();
		if (path != null) {
			LOGGER.finer("Creating a viewer model for path " + path);
		} else {
			LOGGER.finer("Creating a viewer model for a null path");
		}
		return new ViewerModel(map);
	}
}
