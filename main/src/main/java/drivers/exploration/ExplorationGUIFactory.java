package drivers.exploration;

import java.util.Collections;

import java.nio.file.Path;

import javax.swing.SwingUtilities;

import drivers.common.DriverUsage;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.DriverFailedException;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.MultiMapGUIDriver;

import drivers.common.cli.ICLIHelper;

import drivers.gui.common.about.AboutDialog;

import drivers.IOHandler;

import exploration.common.IExplorationModel;
import exploration.common.ExplorationModel;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

import lovelace.util.FileChooser;

import common.map.IMutableMapNG;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.auto.service.AutoService;

/**
 * An factory for the exploration GUI.
 */
@AutoService(DriverFactory.class)
public class ExplorationGUIFactory implements GUIDriverFactory {
	private static final Logger LOGGER = Logger.getLogger(ExplorationGUIFactory.class.getName());
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
	public Iterable<Path> askUserForFiles() {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (FileChooser.ChoiceInterruptedException except) {
			// TODO: throw DriverFailedException
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public GUIDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
		if (model instanceof IExplorationModel) {
			return new ExplorationGUI(cli, options, (IExplorationModel) model);
		} else {
			return createDriver(cli, options, new ExplorationModel(model));
		}
	}

	@Override
	public IDriverModel createModel(IMutableMapNG map) {
		return new ExplorationModel(map);
	}
}
