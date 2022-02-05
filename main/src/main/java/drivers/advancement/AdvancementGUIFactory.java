package drivers.advancement;

import java.util.Collections;
import javax.swing.SwingUtilities;

import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverModel;
import drivers.common.DriverFailedException;
import drivers.common.PlayerChangeListener;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.MultiMapGUIDriver;
import drivers.common.IWorkerModel;
import drivers.common.WorkerGUI;

import drivers.common.cli.ICLIHelper;

import worker.common.WorkerModel;

import drivers.gui.common.about.AboutDialog;

import drivers.PlayerChangeMenuListener;
import drivers.IOHandler;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.MenuBroker;
import drivers.gui.common.SPFileChooser;

import common.map.fixtures.mobile.IUnit;

import java.awt.event.ActionEvent;

import common.map.IMapNG;
import common.map.IMutableMapNG;

import lovelace.util.FileChooser;

import java.nio.file.Path;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.auto.service.AutoService;

/**
 * A factory for the worker-advancemnt GUI app.
 */
@AutoService(DriverFactory.class)
public class AdvancementGUIFactory implements GUIDriverFactory {
	private static final Logger LOGGER = Logger.getLogger(AdvancementGUIFactory.class.getName());
	private static final IDriverUsage USAGE = new DriverUsage(true, "advance", ParamCount.AtLeastOne,
		"Worker Skill Advancement", "View a player's units, the workers in those units, each worker's Jobs, and his or her level in each Skill in each Job.",
		false, true, "--current-turn=NN");

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
		} catch (final FileChooser.ChoiceInterruptedException except) {
			// TODO: throw as DriverFailedException once interface permits
			LOGGER.log(Level.WARNING, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof IWorkerModel) {
			return new AdvancementGUI(cli, options, (IWorkerModel) model);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new WorkerModel(map);
	}
}
