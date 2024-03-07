package drivers.worker_mgmt;

import drivers.common.DriverFailedException;

import java.util.List;
import java.nio.file.Path;

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
import legacy.map.IMutableLegacyMap;
import lovelace.util.FileChooser;
import drivers.gui.common.SPFileChooser;

import com.google.auto.service.AutoService;

/**
 * A factory for the worker management GUI.
 */
@AutoService(DriverFactory.class)
public class WorkerMgmtGUIFactory implements GUIDriverFactory {
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
	public List<Path> askUserForFiles() throws DriverFailedException {
		try {
			return SPFileChooser.open((Path) null).getFiles();
		} catch (final FileChooser.ChoiceInterruptedException except) {
			throw new DriverFailedException(except, "Choice interrupted or user didn't choose");
		}
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IDriverModel model) {
		if (model instanceof final IWorkerModel wm) {
			return new WorkerMgmtGUI(cli, options, wm);
		} else {
			return createDriver(cli, options, new WorkerModel(model));
		}
	}

	@Override
	public IDriverModel createModel(final IMutableLegacyMap map) {
		return new WorkerModel(map);
	}
}
