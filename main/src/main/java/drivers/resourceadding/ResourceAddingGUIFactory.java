package drivers.resourceadding;

import drivers.common.DriverFailedException;

import java.nio.file.Path;

import drivers.common.cli.ICLIHelper;

import java.util.List;

import lovelace.util.FileChooser;

import legacy.map.IMutableMapNG;

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
public class ResourceAddingGUIFactory implements GUIDriverFactory {
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
    public List<Path> askUserForFiles() throws DriverFailedException { // TODO: Make interface declare ChoiceInterruptedException instead of DriverFailedException?
        try {
            return SPFileChooser.open((Path) null).getFiles();
        } catch (final FileChooser.ChoiceInterruptedException except) {
            throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
        }
    }

    @Override
    public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (model instanceof final ResourceManagementDriverModel rmdm) {
            return new ResourceAddingGUI(cli, options, rmdm);
        } else {
            return createDriver(cli, options, new ResourceManagementDriverModel(model));
        }
    }

    @Override
    public IDriverModel createModel(final IMutableMapNG map) {
        return new ResourceManagementDriverModel(map);
    }
}
