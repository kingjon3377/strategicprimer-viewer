package drivers.worker_mgmt;

import java.util.Collections;
import java.util.logging.Level;
import java.nio.file.Path;
import java.util.logging.Logger;
import drivers.common.cli.ICLIHelper;
import drivers.common.IDriverUsage;
import drivers.common.ParamCount;
import drivers.common.DriverUsage;
import drivers.common.SPOptions;
import drivers.common.IDriverModel;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.IWorkerModel;
import worker.common.WorkerModel;
import common.map.IMutableMapNG;
import lovelace.util.FileChooser;
import drivers.gui.common.SPFileChooser;

import com.google.auto.service.AutoService;

/**
 * A factory for the worker management GUI.
 */
@AutoService(DriverFactory.class)
public class WorkerMgmtGUIFactory implements GUIDriverFactory {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerMgmtGUIFactory.class.getName());

	private static final IDriverUsage USAGE = new DriverUsage(true, "worker-mgmt", ParamCount.AtLeastOne,
		"Unit Orders and Worker Management", "Organize the members of a player's units.", false,
		true, "--current-turn=NN", "--print-empty", "--include-unleveled-jobs",
		"--summarize-large-units", "--edit-results");

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
			LOGGER.log(Level.FINE, "Choice interrupted or user didn't choose", except);
			return Collections.emptyList();
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof IWorkerModel) {
			return new WorkerMgmtGUI(cli, options, (IWorkerModel) model);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableMapNG map) {
		return new WorkerModel(map);
	}
}
