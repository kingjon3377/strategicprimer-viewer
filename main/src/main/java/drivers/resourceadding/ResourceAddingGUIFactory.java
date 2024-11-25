package drivers.resourceadding;

import drivers.common.DriverFailedException;

import java.nio.file.Path;

import drivers.common.cli.ICLIHelper;

import java.util.EnumSet;
import java.util.List;

import lovelace.util.FileChooser;

import legacy.map.IMutableLegacyMap;

import drivers.gui.common.SPFileChooser;

import drivers.common.SPOptions;
import drivers.common.GUIDriverFactory;
import drivers.common.DriverUsage;
import drivers.common.DriverFactory;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.IDriverModel;
import drivers.common.GUIDriver;

import com.google.auto.service.AutoService;

/**
 * A factory for the resource-adding GUI app.
 */
@AutoService(DriverFactory.class)
public final class ResourceAddingGUIFactory implements GUIDriverFactory<ResourceManagementDriverModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "add-resource",
			ParamCount.AtLeastOne, "Add resources to maps", "Add resources for players to maps",
			EnumSet.of(IDriverUsage.DriverMode.Graphical), "--current-turn=NN");

	@Override
	public IDriverUsage getUsage() {
		return USAGE;
	}

	/**
	 * Ask the user to choose a file or files.
	 */
	@Override
	// TODO: Make interface declare ChoiceInterruptedException instead of DriverFailedException?
	public List<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options,
	                              final ResourceManagementDriverModel model) {
		return new ResourceAddingGUI(cli, options, model);
	}

	@Override
	public ResourceManagementDriverModel createModel(final IMutableLegacyMap map) {
		return new ResourceManagementDriverModel(map);
	}
}
