package drivers.exploration;

import java.io.IOException;

import drivers.map_viewer.ViewerGUIFactory;

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

import java.util.logging.Logger;
import java.util.logging.Level;

import lovelace.util.FileChooser;

import common.map.IMutableMapNG;

/**
 * An object to start the exploration GUI.
 */
/* package */ class ExplorationGUI implements IExplorationGUI {
	private static final Logger LOGGER = Logger.getLogger(ExplorationGUI.class.getName());
	public ExplorationGUI(ICLIHelper cli, SPOptions options, IExplorationModel model) {
		this.cli = cli;
		this.options = options;
		this.model = model;
	}

	private final ICLIHelper cli;

	private final IExplorationModel model;
	private final SPOptions options;

	@Override
	public IExplorationModel getModel() {
		return model;
	}

	@Override
	public SPOptions getOptions() {
		return options;
	}

	private void createWindow(MenuBroker menuHandler) {
		// FIXME: Try to remove the use of 'this' here
		SPFrame frame = new ExplorationFrame(this, menuHandler);
		frame.addWindowListener(new WindowCloseListener(menuHandler::actionPerformed));
		try {
			menuHandler.registerWindowShower(new AboutDialog(frame,
				frame.getWindowName()), "about");
		} catch (IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error while loading About dialog text", except);
			// But go on anyway
		}
		frame.showWindow();
	}

	@Override
	public void startDriver() {
		MenuBroker menuHandler = new MenuBroker();
		menuHandler.register(new IOHandler(this, new ViewerGUIFactory()::createDriver, cli),
			"load", "save", "save as", "new", "load secondary", "save all",
			"open in map viewer", "open secondary map in map viewer", "close", "quit");
		SwingUtilities.invokeLater(() -> createWindow(menuHandler));
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
	public void open(IMutableMapNG map) {
		if (model.isMapModified()) {
			SwingUtilities.invokeLater(() -> new ExplorationGUI(cli, options,
				new ExplorationModel(map)).startDriver());
		} else {
			model.setMap(map);
		}
	}
}
