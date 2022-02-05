package drivers.resourceadding;

import java.nio.file.Path;

import drivers.common.cli.ICLIHelper;

import lovelace.util.FileChooser;

import common.map.IMutableMapNG;

import drivers.gui.common.SPFileChooser;

import drivers.common.SPOptions;
import drivers.common.GUIDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.IDriverModel;
import drivers.common.GUIDriver;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Collections;

import com.google.auto.service.AutoService;

/**
 * A factory for the resource-adding GUI app.
 */
@AutoService(DriverFactory.class)
public class ResourceAddingGUIFactory implements GUIDriverFactory {
	private static final Logger LOGGER = Logger.getLogger(ResourceAddingGUIFactory.class.getName());
	private static final IDriverUsage USAGE = new DriverUsage(true, "add-resource",
		ParamCount.AtLeastOne, "Add resources to maps", "Add resources for players to maps",
		false, true, "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	public Iterable<Path> askUserForFiles() { // TODO: Make interface declare DriverFailedException instead of just logging it
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof ResourceManagementDriverModel) {
			return new ResourceAddingGUI(cli, options, (ResourceManagementDriverModel) model);
		} else {
			return createDriver(cli, options, new ResourceManagementDriverModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new ResourceManagementDriverModel(map);
	}
}
