package drivers.map_viewer;

import drivers.common.DriverFailedException;
import drivers.common.ViewerDriver;
import drivers.common.ViewerDriverFactory;

import java.util.EnumSet;
import java.util.List;

import java.nio.file.Path;
import java.util.Objects;

import drivers.common.IDriverUsage;
import drivers.common.DriverUsage;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.DriverFactory;
import drivers.common.cli.ICLIHelper;
import legacy.map.IMutableLegacyMap;
import drivers.gui.common.SPFileChooser;

import lovelace.util.FileChooser;

import com.google.auto.service.AutoService;
import lovelace.util.LovelaceLogger;

/**
 * A factory for a driver to start the map viewer.
 */
@AutoService({DriverFactory.class, ViewerDriverFactory.class})
public final class ViewerGUIFactory implements ViewerDriverFactory<IViewerModel> { // TODO: Move type param up a level?
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "view-map",
			ParamCount.One, "Map viewer", "Look at the map visually. This is probably the app you want.",
			EnumSet.of(IDriverUsage.DriverMode.Graphical), "--current-turn=NN", "--background=image.png",
			"--starting-row=NN --starting-column=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	public List<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public ViewerDriver createDriver(final ICLIHelper cli, final SPOptions options, final IViewerModel model) {
		return ViewerGUI.createDriver(cli, options, model);
	}

	@Override
	public IViewerModel createModel(final IMutableLegacyMap map) {
		final Path path = map.getFilename();
		if (Objects.isNull(path)) {
			LovelaceLogger.trace("Creating a viewer model for a null path");
		} else {
			LovelaceLogger.trace("Creating a viewer model for path %s", path);
		}
		return new ViewerModel(map);
	}
}
