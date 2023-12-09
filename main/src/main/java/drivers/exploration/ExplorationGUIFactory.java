package drivers.exploration;

import drivers.common.DriverFailedException;

import java.nio.file.Path;

import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;

import drivers.common.cli.ICLIHelper;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import drivers.gui.common.SPFileChooser;

import java.util.List;

import lovelace.util.FileChooser;

import legacy.map.IMutableMapNG;

import com.google.auto.service.AutoService;

/**
 * An factory for the exploration GUI.
 */
@AutoService(DriverFactory.class)
public class ExplorationGUIFactory implements GUIDriverFactory {
    private static final IDriverUsage USAGE = new DriverUsage(true, "explore", ParamCount.AtLeastOne,
            "Run exploration.",
            "Move a unit around the map, updating the player's map with what it sees.", false,
            true, "--current-turn=NN");

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
    public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
        if (model instanceof final IExplorationModel em) {
            return new ExplorationGUI(cli, options, em);
        } else {
            return createDriver(cli, options, new ExplorationModel(model));
        }
    }

    @Override
    public IDriverModel createModel(final IMutableMapNG map) {
        return new ExplorationModel(map);
    }
}
