package drivers.advancement;

import drivers.common.DriverFailedException;

import drivers.common.IDriverModel;
import drivers.common.ParamCount;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.GUIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.IWorkerModel;

import drivers.common.cli.ICLIHelper;

import java.util.EnumSet;
import java.util.List;

import worker.common.WorkerModel;

import drivers.gui.common.SPFileChooser;

import legacy.map.IMutableLegacyMap;

import lovelace.util.FileChooser;

import java.nio.file.Path;

import com.google.auto.service.AutoService;

/**
 * A factory for the worker-advancemnt GUI app.
 */
@AutoService(DriverFactory.class)
public final class AdvancementGUIFactory implements GUIDriverFactory<IWorkerModel> {
	private static final IDriverUsage USAGE = new DriverUsage(IDriverUsage.DriverMode.Graphical, "advance",
			ParamCount.AtLeastOne, "Worker Skill Advancement",
			"View a player's units, workers in those units, each worker's Jobs, and Skill levels in each Job.",
			EnumSet.of(IDriverUsage.DriverMode.Graphical), "--current-turn=NN");

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
	public IWorkerModel createModel(IDriverModel model) {
		return new WorkerModel(model);
	}

	@Override
	public GUIDriver createDriver(final ICLIHelper cli, final SPOptions options, final IWorkerModel model) {
		return new AdvancementGUI(cli, options, model);
	}

	@Override
	public IWorkerModel createModel(final IMutableLegacyMap map) {
		return new WorkerModel(map);
	}
}
